package com.wix.e2e.http.client.extractors

import akka.http.scaladsl.model._
import com.wix.e2e.http.api.Marshaller.Implicits._
import com.wix.e2e.http.drivers.{HttpClientTransformersTestSupport, SomePayload}
import org.specs2.mutable.Spec
import org.specs2.specification.Scope

class HttpMessageExtractorsTest extends Spec with HttpMessageExtractors {

  trait ctx extends Scope with HttpClientTransformersTestSupport

  "Message Extractors" should {
    "extract response body" should {
      "as unmarshalled JSON" in new ctx {
        HttpResponse(entity = marshaller.marshall(payload)).extractAs[SomePayload] must_=== payload
      }

      "as string" in new ctx {
        HttpResponse(entity = HttpEntity(strBody)).extractAsString must_=== strBody
      }

      "as array of bytes" in new ctx {
        HttpResponse(entity = HttpEntity(someBytes)).extractAsBytes must_=== someBytes
      }
    }

    "extract response entity" should {
      "as unmarshalled JSON" in new ctx {
        HttpEntity(marshaller.marshall(payload)).extractAs[SomePayload] must_=== payload
      }

      "as string" in new ctx {
        HttpEntity(strBody).extractAsString must_=== strBody
      }

      "as array of bytes" in new ctx {
        HttpEntity(someBytes).extractAsBytes must_=== someBytes
      }
    }

    "extract request body" should {
      "as unmarshalled JSON" in new ctx {
        HttpRequest(entity = marshaller.marshall(payload)).extractAs[SomePayload] must_=== payload
      }

      "as string" in new ctx {
        HttpRequest(entity = HttpEntity(strBody)).extractAsString must_=== strBody
      }

      "as array of bytes" in new ctx {
        HttpRequest(entity = HttpEntity(someBytes)).extractAsBytes must_=== someBytes
      }
    }
  }
}

