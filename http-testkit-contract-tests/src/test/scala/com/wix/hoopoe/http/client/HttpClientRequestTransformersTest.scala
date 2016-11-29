package com.wix.hoopoe.http.client

import akka.http.scaladsl.model._
import com.wix.hoopoe.http.WixHttpTestkitResources
import com.wix.hoopoe.http.client.sync._
import com.wix.hoopoe.http.drivers.HttpRequestMatchers._
import com.wix.hoopoe.http.drivers.SomePayload
import com.wixpress.hoopoe.test._
import org.specs2.matcher.Matchers._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class HttpClientRequestTransformersTest extends SpecWithJUnit {

  def randomPair = randomStr -> randomStr
  
  trait ctx extends Scope {
    val request = HttpRequest(uri = Uri("http://localhost"))

    val keyValue1 = randomPair
    val keyValue2 = randomPair
    val keyValue3 = randomPair
    val someBody = randomStr
    val someBytes = randomBytes(100)
    val payload = SomePayload(randomStr, randomStr)
  }


  "HttpClient" should {

    "add parameter to requested URI" in new ctx {
      withParam(keyValue1)(request) must beRequestWith(url = s"http://localhost?${keyValue1._1}=${keyValue1._2}")
    }

    "add multi-value parameters to requested URI" in new ctx {
      withParams("p" -> "1", "p" -> "2")(request) must beRequestWith(url = s"http://localhost?p=1&p=2")
    }

    "chain several add parameters calls together" in new ctx {
      (withParams(keyValue1) and
       withParam(keyValue2))(request) must
        beRequestWith(url = s"http://localhost?${keyValue1._1}=${keyValue1._2}&${keyValue2._1}=${keyValue2._2}")
    }

    "be able to add header to request" in new ctx {
      withHeaders(keyValue1, keyValue2)(request) must beRequestWith(headers = keyValue1, keyValue2)
    }

    "chain more than one add headers calls to request" in new ctx {
      (withHeader(keyValue1) and withHeader(keyValue2))(request) must beRequestWith(headers = keyValue1, keyValue2)
    }

    "add cookie to request" in new ctx {
      withCookie(keyValue1)(request) must beRequestWithCookies(keyValue1)
    }

    "add cookies to request" in new ctx {
      withCookies(keyValue1, keyValue2)(request) must beRequestWithCookies(keyValue1, keyValue2)
    }

    "chain add cookies to request" in new ctx {
      (withCookies(keyValue1) and withCookie(keyValue2))(request) must beRequestWithCookies(keyValue1, keyValue2)
    }

    "add string payload with content type" in new ctx {
      withPayload(someBody, contentType = TextPlain)(request) must beRequestWithBody(someBody)
    }

    "add byte array payload with custom content type" in new ctx {
      val customType = ContentTypes.`application/octet-stream`
      withPayload(someBytes, contentType = customType)(request) must beRequestWithBody(HttpEntity(customType, someBytes))
    }

    "added objects will be converted to json" in new ctx {
      withPayload(payload)(request) must beRequestWithBody(HttpEntity(JsonContent, WixHttpTestkitResources.jsonMapper.writeValueAsString(payload)))
    }

    "add XML as payload" in new ctx {
      withPayload(<element attribute="value"/>)(request) must
        beRequestWithBody(HttpEntity(XmlContent, """<element attribute="value"/>"""))
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

    "have all handlers chained together without loosing data" in new ctx {
      (withParam(keyValue1) and withHeader(keyValue2) and withCookie(keyValue3) and withPayload(payload))(request) must
        beRequestWith(url = s"http://localhost?${keyValue1._1}=${keyValue1._2}") and
        beRequestWith(headers = keyValue2) and
        beRequestWithCookies(keyValue3) and
        beRequestWithBody(HttpEntity(JsonContent, WixHttpTestkitResources.jsonMapper.writeValueAsString(payload)))
    }
  }

}




