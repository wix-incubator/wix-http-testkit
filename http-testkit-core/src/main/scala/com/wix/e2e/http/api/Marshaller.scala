package com.wix.e2e.http.api

import com.wix.e2e.http.api.DefaultMarshaller.HttpTestkitBundledMarshallers
import com.wix.e2e.http.exceptions.MissingMarshallerException
import org.reflections.Reflections

import scala.collection.JavaConverters._
import scala.util.Try

trait Marshaller {
  def unmarshall[T : Manifest](jsonStr: String): T
  def marshall[T](t: T): String
}


object Marshaller {
  object Implicits {
    implicit val marshaller: Marshaller = defaultMarshaller
  }

  private def defaultMarshaller =
    createFirst( ExternalMarshaller.lookup )
      .orElse( createFirst( DefaultMarshaller.lookup ) )
      .getOrElse( new NopMarshaller )

  // todo: add test for failed matcher
  // todo: add more info to user about failed attempts
  private def createFirst(classes: Iterable[Class[_]]): Option[Marshaller] =
    classes.foldLeft( Option.empty[Marshaller] ) {
      case (None, clazz) => newInstance(clazz)
      case (m, _) => m
    }

  private def newInstance(clazz: Class[_]): Option[Marshaller] =
    Try {
          clazz.getConstructor()
               .newInstance()
               .asInstanceOf[Marshaller]
    }.toOption
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
  def lookup: Seq[Class[_]] = {
    new Reflections().getSubTypesOf(classOf[Marshaller]).asScala
                     .filterNot( c => HttpTestkitBundledMarshallers.contains(c.getName) )
                     .toSeq
  }
}

class NopMarshaller extends Marshaller {
  def marshall[T](t: T): String = throwMissingMarshallerError
  def unmarshall[T: Manifest](jsonStr: String): T = throwMissingMarshallerError

  private def throwMissingMarshallerError = throw new MissingMarshallerException
}
