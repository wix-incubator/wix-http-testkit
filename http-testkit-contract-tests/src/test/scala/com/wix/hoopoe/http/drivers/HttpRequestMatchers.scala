package com.wix.hoopoe.http.drivers

import akka.http.scaladsl.model.headers.{HttpCookiePair, RawHeader}
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, RequestEntity, Uri}
import org.specs2.matcher.Matcher
import org.specs2.matcher.Matchers._

object HttpRequestMatchers {
  def beRequestWith(url: String): Matcher[HttpRequest] = be_===(url) ^^ { (_: HttpRequest).uri.toString aka "request uri" }
  def beRequestWithBody(entity: RequestEntity): Matcher[HttpRequest] = be_===(HttpRequest(entity = entity)) ^^^ { (_: HttpRequest).copy(uri = Uri("http://localhost"), headers = Nil) }
  def beRequestWith(headers: (String, String)*): Matcher[HttpRequest] = contain(allOf[HttpHeader](headers.map(h => RawHeader(h._1, h._2)):_*)) ^^ { (_: HttpRequest).headers aka "request headers" }
  def beRequestWithCookies(cookies: (String, String)*): Matcher[HttpRequest] = contain(allOf(cookies.map(c => HttpCookiePair(c._1, c._2)):_*)) ^^ { (_: HttpRequest).cookies aka "request cookies" }

//  def beHttpRequest(body: Matcher[String] = any,
//                    entity: Matcher[HttpEntity] = any,
//                    headers: Matcher[Traversable[HttpHeader]] = any): Matcher[HttpRequest] = {
////    body ^^ { (_: HttpRequest).entity.asString(HttpCharsets.`UTF-8`) aka "body" } and
//    headers ^^ { (_: HttpRequest).headers aka "headers" }
//  }
//  def beRequestWithBody(body: Array[Byte]): Matcher[HttpRequest] = beEqualTo(body) ^^ { (_: HttpRequest).entity.data.toByteArray }
//  def beRequestWithContentType(contentType: String): Matcher[HttpRequest] = beEqualTo(contentType) ^^ { (_: HttpRequest).entity.toOption.get.contentType.toString() }
}

case class SomePayload(key: String, value: String)
