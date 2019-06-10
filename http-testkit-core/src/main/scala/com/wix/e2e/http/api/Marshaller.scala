package com.wix.e2e.http.api

import com.wix.e2e.http.exceptions.MissingMarshallerException

import scala.util.control.Exception.handling

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

  private def createFirst(classes: Iterable[Class[_]]): Option[Marshaller] =
    classes.foldLeft( Option.empty[Marshaller] ) {
      case (None, clazz) => newInstance(clazz)
      case (m, _) => m
    }

  private def newInstance(clazz: Class[_]): Option[Marshaller] =
    handling(classOf[Exception])
      .by( { _ =>
        println(s"[ERROR]: Failed to create marshaller instance [$clazz].")
        None
      }) {
        Some(clazz.getConstructor()
                  .newInstance()
                  .asInstanceOf[Marshaller])
    }
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

class NopMarshaller extends Marshaller {
  def marshall[T](t: T): String = throwMissingMarshallerError
  def unmarshall[T: Manifest](jsonStr: String): T = throwMissingMarshallerError

  private def throwMissingMarshallerError = throw new MissingMarshallerException
}
