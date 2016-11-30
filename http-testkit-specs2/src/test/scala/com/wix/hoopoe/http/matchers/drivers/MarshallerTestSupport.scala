package com.wix.hoopoe.http.matchers.drivers

import com.wix.hoopoe.http.matchers.json.Marshaller
import org.specs2.matcher.ThrownExpectations
import org.specs2.mock.Mockito

trait MarshallerTestSupport extends Mockito with ThrownExpectations {
  val marshaller: Marshaller = mock[Marshaller]

  def givenUnmarshallerWith[T : Manifest](someEntity: T, forContent: String): Unit = {
    marshaller.unmarshall[T](forContent) returns someEntity
  }

  def givenBadlyBehavingUnmarshallerFor[T : Manifest](withContent: String): Unit = {
    marshaller.unmarshall[T](withContent) throws new RuntimeException
  }
}

trait CustomMarshallerProvider {
  def marshaller: Marshaller
  implicit def customMarshaller: Marshaller = marshaller
}

