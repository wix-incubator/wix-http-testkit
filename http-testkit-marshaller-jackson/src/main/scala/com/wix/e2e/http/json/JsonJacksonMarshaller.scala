package com.wix.e2e.http.json

import java.lang.reflect.{ParameterizedType, Type}

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.wix.e2e.http.api.Marshaller

class JsonJacksonMarshaller extends Marshaller {

  def unmarshall[T : Manifest](jsonStr: String): T = objectMapper.readValue(jsonStr, typeReference[T])
  def marshall[T](t: T): String = objectMapper.writeValueAsString(t)

  def configure: ObjectMapper = objectMapper

  private val objectMapper = JsonMapper.builder
                                       .addModule(new Jdk8Module())
                                       .addModules(new JodaModule, new ParameterNamesModule, new JavaTimeModule)
                                       .addModule(new DefaultScalaModule)
                                       .disable( WRITE_DATES_AS_TIMESTAMPS )
                                       .build()

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
