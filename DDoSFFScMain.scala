package com.seceon.ddos

import kafka.serializer.StringDecoder
import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.streaming.kafka._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import scala.math._

object DDoSFFScMain {
  var vectors = List[List[Double]]()
  var fvectors = List[FeatureInfo]()
  var FFSc = List[Double]()

  val logger = LogManager.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    /**
      * Setup Log Level
      */
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)
    Logger.getRootLogger.setLevel(Level.OFF)

    /** Defining Constants
      * Todo : we can parse the constants from input in future for flexibility
      */
    val MicroBatchSize: Int = 10

    /**
      * Spark Configuration.
      **/

    lazy val conf = new SparkConf()
      .set("streaming.batch.duration.sec", MicroBatchSize.toString)
      .setMaster("local[2]")
      .setAppName("seceon-ddos-ffsc")
      .set("spark.hadoop.validateOutputSpecs", "false")

    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(MicroBatchSize))

    val kafkaParams = Map[String, String](
      "bootstrap.servers" -> "sape-kafka-1:9092")

    val netflowsDStream = {

      KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
        ssc, kafkaParams, "netflow-enriched".split(",").toSet).map(_._2)
    }

    // Read raw netflow and convert it to NetflowMessage object
    val netflowMessageStream = netflowsDStream.map { nf => {
      lazy val nfm_obj = {
        try {
          NetflowMessageJsonHandler.fromJson(nf)
          //mapper.readValue(nf, classOf[Map[String,_]])
        } catch {
          case e: Exception => {
            println("Error: Failed to parse netflow. " + e.getMessage)
          }
            null
        }
      }
      nfm_obj
    }
    }.filter(x => !Option(x).isEmpty)
      .filter(x => x.!=(None))
      .filter(x => {
        //TODO Add code for filtering unwanted Netflow
        true
      })
    netflowMessageStream.glom()
    netflowMessageStream.cache()
    netflowMessageStream.print()

    /**
      * Data Instance and Source Ip Count Calculation
      **/

    val dataCount = netflowMessageStream.map(x => ("flow", 1)).reduceByKey(_ + _)
    println("Print Data count")
    dataCount.print()

    try {
      // print("IN try")
      //if (dataCount.asInstanceOf[Int] > 0) {
      val srcIpCount = netflowMessageStream.map(x => {
        val nf = (x.get("netflow").get)
        (nf.asInstanceOf[scala.collection.immutable.HashMap[String, Object]].get("ipv4_src_addr").get, 1)
      }) reduceByKey (_ + _)
      dataCount.print()
      //srcIpCount.print()

      /**
        * Variation of Source IP Calculation
        **/
      val uniqueSrcIPcount = srcIpCount.map(x => ("flow", 1)).reduceByKey(_ + _)
      //uniqueSrcIPcount.print()

      val variation1 = uniqueSrcIPcount.join(dataCount).map(x => 100 * float2Float(x._2._1) / float2Float(x._2._2))
      //variation1.print()

      val mean = uniqueSrcIPcount.join(dataCount).map(x => float2Float(x._2._2) / float2Float(x._2._1)).map(x => ("flow", x))
      //mean.print()
      val stdDev = srcIpCount.map(x => ("flow", x._2)).join(mean)
        .map(x => ("flow", pow(x._2._1 - x._2._2, 2))).reduceByKey(_ + _).join(uniqueSrcIPcount)
        .map(x => ("flow", sqrt(x._2._1 / x._2._2 - 1)))
      //stdDev.print()

      val variation2 = stdDev.join(mean).map(x => x._2._1.asInstanceOf[Double] / x._2._2.asInstanceOf[Double])
      variation2.print()

      /**
        * Entropy Calculation
        **/
//
      val entropy = srcIpCount.map(x => ("flow", x._2)).join(dataCount)
        .map(x => float2Float(x._2._1) / float2Float(x._2._2) * log10(float2Float(x._2._1) / float2Float(x._2._2)) / log10(2) * -1)
        .map(x => ("entropy", x))
        .reduceByKey(_ + _).map(x => x._2.asInstanceOf[Double])

      entropy.print()
      /**
        * Packet Rate Calculation
        **/
      val durationSumSec = netflowMessageStream.map(x => {
        (x.get("duration").get)
      }).map(x => ("flow", x.asInstanceOf[Int]))
        .reduceByKey(_ + _).map(x => (x._1, x._2.asInstanceOf[Int] / 1000))
      //durationSumSec.print()

      val inPktsSum = netflowMessageStream.map(x => {
        val nf = (x.get("netflow").get)
        (nf.asInstanceOf[scala.collection.immutable.HashMap[String, Object]].get("in_pkts").get)
      }).map(x => ("flow", x.asInstanceOf[Int]))
        .reduceByKey(_ + _)
      //inPktsSum.print()

      val packetRate = inPktsSum.join(durationSumSec)
        .map(x => x._2._1.asInstanceOf[Double] / x._2._2.asInstanceOf[Double])
      packetRate.print()

      val classobj = new FeatureInfo(entropy, variation2, packetRate)
      fvectors ::= classobj
      for(i<-1 to fvectors.length)
        FFSc ::=fvectors(i).calcFFSc()
      print(FFSc)
      print(fvectors)
      classobj.showValues()
      // }
    } catch {
      case e: Exception => {
        println("Error: No Data. " + e.getMessage)
      }

    }
    ssc.start()
    ssc.awaitTermination()

  }
}