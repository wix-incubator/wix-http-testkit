package com.wix.e2e.http.matchers.drivers

import com.wix.e2e.http.api.Marshaller
import org.scalamock.scalatest.MockFactory

trait MarshallerTestSupport extends MockFactory {
  val marshaller: Marshaller = mock[Marshaller]

  def givenUnmarshallerWith[T : Manifest](someEntity: T, forContent: String)(implicit mn: Manifest[T]): Unit =
    (marshaller.unmarshall[T] (_: String) (_: Manifest[T]) ).expects(forContent, *).returning(someEntity).repeat(1 to 3)

  def givenBadlyBehavingUnmarshallerFor[T : Manifest](withContent: String): Unit =
    (marshaller.unmarshall[T] (_: String) (_: Manifest[T]) ).expects(withContent, *).throwing(new RuntimeException)
}


trait CustomMarshallerProvider {
  def marshaller: Marshaller
  implicit def customMarshaller: Marshaller = marshaller
}
