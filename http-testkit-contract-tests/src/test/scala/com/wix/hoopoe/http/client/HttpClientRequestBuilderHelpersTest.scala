package com.wix.hoopoe.http.client

//import com.wix.e2e.http.internals.HttpClientRequestBuilderHelpers._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import com.wix.hoopoe.http.client.HttpRequestMatchers._
import com.wix.hoopoe.http.client.sync._
import com.wixpress.hoopoe.test._
import org.specs2.matcher.Matchers._
import org.specs2.matcher.{AlwaysMatcher, Matcher}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class HttpClientRequestTransformersTest extends SpecWithJUnit {

  def randomPair = randomStr -> randomStr
  
  trait ctx extends Scope {
    val request = HttpRequest(uri = Uri("http://localhost"))

    val keyValue1 = randomPair
    val keyValue2 = randomPair
    val someBody = randomStr
    val someBytes = randomBytes(100)
    val payload = SomePayload(randomStr, randomStr)
  }


  "HttpClientSupport" should {

//    "be able to add XML as payload" in new ctx {
//      withPayload(<element attribute="value"/>)(request) must beHttpRequest(
//        entity = be_===(HttpEntity(ContentType(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`), """<element attribute="value"/>""")))
//    }
//
    "be able to add parameters to requested URI" in new ctx {
      withParams(keyValue1)(request) must beRequestWith(url = s"http://localhost?${keyValue1._1}=${keyValue1._2}")
    }

    "be able to add multi-value parameters to requested URI" in new ctx {
      withParams("p" -> "1", "p" -> "2")(request) must beRequestWith(url = s"http://localhost?p=1&p=2")
    }
//
//    "be able to chain several add parameters together" in new ctx {
//      (withParams(keyValue1) and
//      withParams(keyValue2)) (request) must
//        beRequestWith(url = s"http://localhost?${keyValue1._1}=${keyValue1._2}&${keyValue2._1}=${keyValue2._2}")
//    }
//
//    "be able to add headers to request" in new ctx {
//      withHeaders(keyValue1, keyValue2)(request) must beRequestWith(headers = keyValue1, keyValue2)
//    }
//
//    "be able to chain headers to request" in new ctx {
//      (withHeaders(keyValue1) and withHeaders(keyValue2))(request) must beRequestWith(headers = keyValue1, keyValue2)
//    }
//
//    "be able to add cookies to request" in new ctx {
//      withCookies(keyValue1, keyValue2)(request) must beRequestWithCookies(keyValue1, keyValue2)
//    }
//
//    "be able to chain cookies to request" in new ctx {
//      (withCookies(keyValue1) and withCookies(keyValue2))(request) must beRequestWithCookies(keyValue1, keyValue2)
//    }
//
//    "add string payload with content type" in new ctx {
//      withPayload(someBody, contentType = TextPlain)(request) must beRequestWithBody(someBody)
//    }
//
//    "add byte array payload with custom content type" in new ctx {
//      val customType = ContentTypes.`application/octet-stream`
//      withPayload(someBytes, contentType = customType)(request) must (
//        beRequestWithBody(someBytes) and
//        beRequestWithContentType("application/octet-stream")
//      )
//    }
//
//    "added objects will be converted to json" in new ctx {
//      withPayload(payload)(request) must beRequestWithBody(payload.asJsonStr)
//    }
//
//    "be able to add accept header with text/plain value" in new ctx {
//      withAcceptTextPlainHeader(request) must beRequestWith("Accept" -> "text/plain; charset=UTF-8")
//    }
//
//    "add form url encoded payload" in new ctx {
//      withFormData("param1"->"value1&value1 space", "param2"->"//")(request) must
//        (beRequestWithBody("param1=value1%26value1+space&param2=%2F%2F") and
//          beRequestWithContentType( "application/x-www-form-urlencoded; charset=UTF-8"))
//    }
//
//    "withFormData should keep previous headers" in new ctx {
//      (withHeaders(keyValue1) and withFormData(keyValue2))(request) must
//        (beRequestWith(headers = keyValue1) and beRequestWithBody(s"${keyValue2._1}=${keyValue2._2}"))
//    }
  }

}

case class SomePayload(key: String, value: String)

object HttpRequestMatchers {

  private def any[T] = AlwaysMatcher[T]()

  def beHttpRequest(body: Matcher[String] = any,
                    entity: Matcher[HttpEntity] = any,
                    headers: Matcher[Traversable[HttpHeader]] = any): Matcher[HttpRequest] = {
//    body ^^ { (_: HttpRequest).entity.asString(HttpCharsets.`UTF-8`) aka "body" } and
    headers ^^ { (_: HttpRequest).headers aka "headers" }
  }

  def beRequestWith(url: String): Matcher[HttpRequest] = be_===(url) ^^ { (_: HttpRequest).uri.toString aka "request uri" }
//  def beRequestWithBody(body: String): Matcher[HttpRequest] = beEqualTo(body) ^^ { (_: HttpRequest).entity.asString(`UTF-8`) }
//  def beRequestWithBody(body: Array[Byte]): Matcher[HttpRequest] = beEqualTo(body) ^^ { (_: HttpRequest).entity.data.toByteArray }
  def beRequestWith(headers: (String, String)*): Matcher[HttpRequest] = containTheSameElementsAs[HttpHeader](headers.map(h => RawHeader(h._1, h._2))) ^^ { (_: HttpRequest).headers }
//  def beRequestWithCookies(cookies: (String, String)*): Matcher[HttpRequest] = containTheSameElementsAs[HttpCookie](cookies.map(c => HttpCookie(c._1, c._2))) ^^ { (_: HttpRequest).cookies }
//  def beRequestWithContentType(contentType: String): Matcher[HttpRequest] = beEqualTo(contentType) ^^ { (_: HttpRequest).entity.toOption.get.contentType.toString() }
}
