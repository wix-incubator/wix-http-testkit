package com.wix.e2e.http.drivers

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.drivers.MarshallingTestObjects.SomeCaseClass
import com.wix.test.random.{randomInt, randomStr}

import scala.collection.concurrent.TrieMap

trait MarshallerTestSupport {
  val someObject = SomeCaseClass(randomStr, randomInt)
  val content = randomStr

  def givenMarshallerThatUnmarshalWith(unmarshal: SomeCaseClass, forContent: String): Unit =
    MarshallingTestObjects.unmarshallResult.put(forContent, unmarshal)

  def givenMarshallerThatMarshal(content: String, to: SomeCaseClass): Unit =
    MarshallingTestObjects.marshallResult.put(to, content)

  def aResponseWith(body: String) = HttpResponse(entity = body)
  def aRequestWith(body: String) = HttpRequest(entity = body)
  val request = HttpRequest()
}

object MarshallingTestObjects {
  case class SomeCaseClass(s: String, i: Int)

  val marshallResult = TrieMap.empty[SomeCaseClass, String]
  val unmarshallResult = TrieMap.empty[String, SomeCaseClass]

  class MarshallerForTest extends Marshaller {

    def unmarshall[T: Manifest](jsonStr: String) =
      MarshallingTestObjects.unmarshallResult
                            .getOrElse(jsonStr, throw new UnsupportedOperationException)
                            .asInstanceOf[T]

    def marshall[T](t: T) =
      MarshallingTestObjects.marshallResult
                            .getOrElse(t.asInstanceOf[SomeCaseClass], throw new UnsupportedOperationException)
  }
}

