package com.seceon.ddos

import org.apache.spark.streaming.dstream.DStream

import scala.math._

class FeatureInfo (e:DStream[Double], v:DStream[Double], p:DStream[Double]) {
  val entropyVal=e.asInstanceOf[Double]
  val variationSrcIPVal=v.asInstanceOf[Double]
  val packetRateVal=p.asInstanceOf[Double]

  val obj = List(entropyVal,variationSrcIPVal,packetRateVal)

  def showValues()={
    println("Entropy value: " + entropyVal)
    println("VariationSrcIP value: " + variationSrcIPVal)
    println("packetRate value: " + packetRateVal)
  }

  def calcMean():Double ={
    val meanVal = obj.sum/obj.length
    return meanVal
  }

  def calcFFSc(): Double ={
    val f1 = abs(entropyVal - variationSrcIPVal) + abs(entropyVal - packetRateVal)
    val f2 = abs(variationSrcIPVal - entropyVal) + abs(variationSrcIPVal - packetRateVal)
    val f3 = abs(packetRateVal - entropyVal) + abs(packetRateVal - variationSrcIPVal)
    val FFoR = List(f1, f2, f3)
    val AFFoR = FFoR.sum / FFoR.length
    val df1 = f1 - AFFoR
    val df2 = f2 - AFFoR
    val df3 = f3 - AFFoR
    val Dev = List(df1, df2, df3)
    val meanDev = Dev.sum / Dev.length

    val product = (entropyVal * df1) + (variationSrcIPVal * df2) + (packetRateVal * df3)
    val FFSc = product / (calcMean() + meanDev)
    return FFSc
  }

  def calculateFeatures(netflowMessageStream:Map[String,Object] )={
    println("Calculating entropy, variation and packet rate")
    //val dataCount = netflowMessageStream.map( x => ("flow",1)).reduceByKey(_ + _)
    //println("Print Data count: " + dataCount)

    /*val srcIpCount = netflowMessageStream.map( x => {
      val nf= (x.get("netflow").get)
      (nf.asInstanceOf[ scala.collection.immutable.HashMap[String, Object]].get("ipv4_src_addr").get , 1)
    })reduceByKey(_ + _)

    val entropy = srcIpCount.map(x => ("flow",x._2)).join(dataCount)
      .map(x => ("entropy",float2Float(x._2._1)/float2Float(x._2._2) * log10(float2Float(x._2._1)/float2Float(x._2._2)) / log10(2) * -1))
      .reduceByKey(_+_)
    println("entropy: " + entropy)

    val durationSumSec = netflowMessageStream.map( x => {
      (x.get("duration").get) }).map(x=>("flow",x.asInstanceOf[Int]))
      .reduceByKey(_ + _).map(x=> (x._1,x._2.asInstanceOf[Int] / 1000))
    println("DurationSum: " + durationSumSec)

    val inPktsSum = netflowMessageStream.map( x => {
      val nf= (x.get("netflow").get)
      (nf.asInstanceOf[ scala.collection.immutable.HashMap[String, Object]].get("in_pkts").get)
    }).map(x=>("flow",x.asInstanceOf[Int])).reduceByKey(_+_)
    println("inPktsSum: " + inPktsSum)

    val packetRate = inPktsSum.join(durationSumSec).map(x=> x._2._1.asInstanceOf[Float] / x._2._2.asInstanceOf[Float])
    println("PacketRate: " + packetRate)*/

  }
}
