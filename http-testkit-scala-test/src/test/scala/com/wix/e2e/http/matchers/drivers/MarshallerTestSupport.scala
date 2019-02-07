package com.wix.e2e.http.matchers.drivers

import com.wix.e2e.http.api.Marshaller

import scala.collection.concurrent.TrieMap
import scala.language.reflectiveCalls

trait MarshallerTestSupport {
  val marshaller = new Marshaller {
    val unmarshallResult = TrieMap.empty[String, AnyRef]
    val unmarshallError = TrieMap.empty[String, Throwable]

    def unmarshall[T: Manifest](jsonStr: String) = {
      unmarshallError.get(jsonStr).foreach( throw _ )
      unmarshallResult.getOrElse(jsonStr, throw new UnsupportedOperationException)
                      .asInstanceOf[T]
    }

    def marshall[T](t: T) = ???
  }

  def givenUnmarshallerWith[T <: AnyRef](someEntity: T, forContent: String)(implicit mn: Manifest[T]): Unit =
    marshaller.unmarshallResult.put(forContent, someEntity)

  def givenBadlyBehavingUnmarshallerFor[T : Manifest](withContent: String): Unit =
    marshaller.unmarshallError.put(withContent, new RuntimeException)
}

trait CustomMarshallerProvider {
  def marshaller: Marshaller
  implicit def customMarshaller: Marshaller = marshaller
}
