package com.seceon.ddos

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule


class Jsonizer {

}

trait JsonHandler[T] {
  def toJson(obj: T, pretty: Boolean = false): String
  def fromJson(json: String): T

}

abstract class JacksonJsonHandler[T] extends Serializable with JsonHandler[T] {
  private val mapper = new ObjectMapper() //with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.setSerializationInclusion(Include.NON_NULL);
  mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
  mapper.setSerializationInclusion(Include.NON_EMPTY)


  override def toJson(obj: T, pretty: Boolean = false): String = {
    if (pretty) mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
    else mapper.writeValueAsString(obj)
  }

  def fromJson(json: String, clazz: Class[T]): T = {
    mapper.readValue(json, clazz)
  }

  def toMap(json: String): Map[String, String] = {
    mapper.readValue(json, classOf[Map[String, String]])
  }
}

object NetflowMessageJsonHandler extends JacksonJsonHandler[Map[String, Any]] {
  override def fromJson(json: String): Map[String, Any] = fromJson(json, classOf[Map[String, Any]])
}