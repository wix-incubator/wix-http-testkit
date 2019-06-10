package com.wix.e2e.http.api

import com.wix.e2e.http.api.DefaultMarshaller.HttpTestkitBundledMarshallers
import org.reflections.Reflections

import scala.jdk.CollectionConverters._


object ExternalMarshaller {
  def lookup: Seq[Class[_]] = {
    new Reflections().getSubTypesOf(classOf[Marshaller]).asScala
                     .filterNot( c => HttpTestkitBundledMarshallers.contains(c.getName) )
                     .toSeq
  }
}
