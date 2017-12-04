package com.wix.e2e.http.api

import com.wix.e2e.http.api.DefaultMarshaller.HttpTestkitBundledMarshallers
import com.wix.e2e.http.exceptions.MissingMarshallerException
import org.reflections.Reflections

import scala.collection.JavaConverters._

trait Marshaller {
  def unmarshall[T : Manifest](jsonStr: String): T
  def marshall[T](t: T): String
}


object Marshaller {
  object Implicits {
    implicit val marshaller: Marshaller = defaultMarshaller
  }

  private def defaultMarshaller =
    ExternalMarshaller.lookup
                      .orElse( DefaultMarshaller.lookup )
                      .map( newInstance )
                      .getOrElse( new NopMarshaller )

  private def newInstance(clazz: Class[_]) =
    clazz.getConstructor()
         .newInstance()
         .asInstanceOf[Marshaller]
}

object DefaultMarshaller {
  val DefaultMarshallerClassName = "com.wix.e2e.http.json.JsonJacksonMarshaller"
  val HttpTestkitBundledMarshallers = Seq(DefaultMarshallerClassName, classOf[NopMarshaller].getName)

  def lookup: Option[Class[_]] =
    try {
      Option(Class.forName(DefaultMarshallerClassName))
    } catch {
      case _: Exception => None
    }
}

object ExternalMarshaller {
  def lookup: Option[Class[_]] = {
    new Reflections().getSubTypesOf(classOf[Marshaller]).asScala
                     .filterNot( c => HttpTestkitBundledMarshallers.contains(c.getName) )
                     .headOption
  }
}

class NopMarshaller extends Marshaller {
  def marshall[T](t: T): String = throwMissingMarshallerError
  def unmarshall[T: Manifest](jsonStr: String): T = throwMissingMarshallerError

  private def throwMissingMarshallerError = throw new MissingMarshallerException
}
