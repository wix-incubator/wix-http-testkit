package com.wix.e2e.http.api

import com.wix.e2e.http.exceptions.MissingMarshallerException
import org.reflections.Reflections

import scala.collection.JavaConverters._

trait Marshaller {
  def unmarshall[T : Manifest](jsonStr: String): T
  def marshall[T](t: T): String
}


object Marshaller {

  implicit val marshaller: Marshaller = defaultMarshaller

  private def defaultMarshaller =
    DefaultMarshaller.lookup
                     .orElse( ExternalMarshaller.lookup )
                     .map( newInstance )
                     .getOrElse( new NopMarshaller )

  private def newInstance(clazz: Class[_]) =
    clazz.getConstructor()
         .newInstance()
         .asInstanceOf[Marshaller]
}

object DefaultMarshaller {
  def lookup: Option[Class[_]] =
    try {
      Option(Class.forName("com.wix.e2e.http.json.JsonJacksonMarshaller"))
    } catch {
      case _: Exception => None
    }
}

object ExternalMarshaller {
  def lookup: Option[Class[_]] =
    new Reflections().getSubTypesOf(classOf[Marshaller]).asScala
                     .filterNot( _ == classOf[NopMarshaller])
                     .headOption
}

class NopMarshaller extends Marshaller {
  def marshall[T](t: T): String = throwMissingMarshallerError
  def unmarshall[T: Manifest](jsonStr: String): T = throwMissingMarshallerError

  private def throwMissingMarshallerError = throw new MissingMarshallerException
}