package com.wix.e2e.http.client

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.HttpCookiePair
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.client.transformers.HttpClientTransformers
import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.test.random._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class HttpClientTransformersTest extends SpecWithJUnit with HttpClientTransformers {

  trait ctx extends Scope {
    val request = HttpRequest()

    val keyValue1 = randomStrPair
    val keyValue2 = randomStrPair
    val keyValue3 = randomStrPair
    val someBody = randomStr
    val someBytes = randomBytes(100)
    val payload = SomePayload(randomStr, randomStr)
  }


  "RequestTransformers" should {

    "add parameter to requested URI" in new ctx {
      withParam(keyValue1)(request) must haveTheSameParamsAs(keyValue1)
    }

    "add multi-value parameters to requested URI" in new ctx {
      withParams(keyValue1, keyValue2)(request) must haveTheSameParamsAs(keyValue1, keyValue2)
    }

    "chain several add parameters calls together" in new ctx {
      (withParams(keyValue1) and
       withParam(keyValue2))(request) must
        haveTheSameParamsAs(keyValue1, keyValue2)
    }

    "be able to add header to request" in new ctx {
      withHeaders(keyValue1, keyValue2)(request) must haveTheSameHeadersAs(keyValue1, keyValue2)
    }

    "chain more than one add headers calls to request" in new ctx {
      (withHeader(keyValue1) and
       withHeader(keyValue2))(request) must haveTheSameHeadersAs(keyValue1, keyValue2)
    }

    "add cookie to request" in new ctx {
      withCookie(keyValue1)(request) must receivedCookieThat(be_===(HttpCookiePair(keyValue1)))
    }

    "add cookies to request" in new ctx {
      withCookies(keyValue1, keyValue2)(request) must { receivedCookieThat(be_===(HttpCookiePair(keyValue1))) and
                                                        receivedCookieThat(be_===(HttpCookiePair(keyValue2))) }
    }

    "chain add cookies to request" in new ctx {
      (withCookies(keyValue1) and
       withCookie(keyValue2))(request) must { receivedCookieThat(be_===(HttpCookiePair(keyValue1))) and
                                              receivedCookieThat(be_===(HttpCookiePair(keyValue2))) }
    }

    "add string payload with content type" in new ctx {
      withPayload(someBody, contentType = TextPlain)(request) must haveBodyWith(someBody)
      // todo: fix this
//      withPayload(someBody)(request) must haveBodyWith(someBody)
    }

    "add byte array payload with custom content type" in new ctx {
      val customType = ContentTypes.`application/octet-stream`
      withPayload(someBytes, contentType = customType)(request) must haveBodyWith(someBytes)
    }

    "added objects will be converted to json" in new ctx {
      withPayload(payload).apply(request) must havePayloadWith(payload)
    }

    "add XML as payload" in new ctx {
      withPayload(<element attribute="value"/>)(request) must haveBodyWith("""<element attribute="value"/>""")
    }

//    "add form url encoded payload" in new ctx {
//      withFormData("param1"->"value1&value1 space", "param2"->"//")(request) must
//        beRequestWithBody(HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`, `UTF-8`), "param1=value1%26value1+space&param2=%2F%2F"))
//    }.pendingUntilFixed("figure how marshallers work in akka http")
//
//    "withFormData should keep previous headers" in new ctx {
//      (withHeaders(keyValue1) and withFormData(keyValue2))(request) must
//        beRequestWithBody(HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`, `UTF-8`), "param1=value1%26value1+space&param2=%2F%2F"))
//    }.pendingUntilFixed("figure how marshallers work in akka http")
//
    "have all handlers chained together without loosing data" in new ctx {
      (withParam(keyValue1) and
       withHeader(keyValue2) and
       withCookie(keyValue3) and
       withPayload(payload))(request) must
        { haveTheSameParamsAs(keyValue1) and
          haveAllOf(keyValue2) and
          receivedCookieThat(be_===(HttpCookiePair(keyValue3))) and
          havePayloadWith(payload) }
    }

  }

  "ResponseTransformers" should {
    "easily extract content from response" in new ctx {
      HttpResponse(entity = Marshaller.marshaller.marshall(payload)).extractAs[SomePayload] must_=== payload
    }
  }
}


case class SomePayload(key: String, value: String)

