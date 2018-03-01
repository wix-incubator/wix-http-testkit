package com.wix.e2e.http.filters

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, Uri}
import com.wix.e2e.http.api.Marshaller
import org.specs2.mutable.Spec

class BodyFiltersTest extends Spec with BodyFilters {

  "forBody" should {
    "unmarshall body and match it with provided matcher" in {
      val data = SomeData("1")
      matchBody(data, data) must beTrue
      matchBody(data, SomeData("2")) must beFalse
    }

    "be false when unmarshall fails" in {
      matchBody(SomeData("1"), SomeData("2"), emulateUnmarshallFailure = true) must beFalse
    }
  }

  def matchBody(actual: SomeData, expected: SomeData, emulateUnmarshallFailure: Boolean = false): Boolean = {
    implicit val marshaller = new Marshaller {
      override def marshall[T](t: T): String = actual.toString
      override def unmarshall[T: Manifest](jsonStr: String): T =
        if (!emulateUnmarshallFailure) actual.asInstanceOf[T] else throw new IllegalArgumentException("Can't unmarshall")
    }
    val filter = forBody({ _ == expected }: BodyMatcher[SomeData])
    filter(HttpRequest(entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, actual.toString)))
  }
}

case class SomeData(data: String)
