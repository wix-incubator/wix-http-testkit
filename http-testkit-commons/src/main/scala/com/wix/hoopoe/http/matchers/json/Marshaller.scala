package com.wix.hoopoe.http.matchers.json

import java.lang.reflect.{ParameterizedType, Type}

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.MapperFeature.USE_ANNOTATIONS
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.datatype.jsr310.JSR310Module
import com.fasterxml.jackson.module.scala.DefaultScalaModule

trait Marshaller {
  def unmarshall[T : Manifest](jsonStr: String): T
  def marshall[T](t: T): String
}

class JsonJacksonMarshaller extends Marshaller {

  def unmarshall[T : Manifest](jsonStr: String): T = objectMapper.readValue(jsonStr, typeReference[T])
  def marshall[T](t: T): String = objectMapper.writeValueAsString(t)

  private val objectMapper = new ObjectMapper()
                                    .registerModules(new DefaultScalaModule)
                                    .registerModules(new JodaModule, new Jdk8Module, new JSR310Module) // time modules
                                    .disable( WRITE_DATES_AS_TIMESTAMPS )
                                    .disable( USE_ANNOTATIONS )

  private def typeReference[T: Manifest] = new TypeReference[T] {
    override def getType = typeFromManifest(manifest[T])
  }

  private def typeFromManifest(m: Manifest[_]): Type = {
    if (m.typeArguments.isEmpty) { m.runtimeClass }
    else new ParameterizedType {
      def getRawType = m.runtimeClass
      def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray
      def getOwnerType = null
    }
  }
}

object DefaultMarshaller {
  val marshaller: Marshaller = new JsonJacksonMarshaller
}



//object HttpTestkitJacksonMarshaller {
//  private val globalObjectMapper = new ObjectMapper
//
//  private def customizeObjectMapper(objectMapper: ObjectMapper): Unit = ???
//  private def registerModules(): Unit = ???
//
//  private def mapper: ObjectMapper = globalObjectMapper
//}


