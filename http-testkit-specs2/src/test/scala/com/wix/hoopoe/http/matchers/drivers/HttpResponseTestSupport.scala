package com.wix.hoopoe.http.matchers.drivers

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.{HttpCookie, RawHeader, `Set-Cookie`}
import com.wixpress.hoopoe.test.randomStr

import scala.collection.immutable

trait HttpResponseTestSupport {

  val cookie = randomCookie
  val anotherCookie = randomCookie
  val yetAnotherCookie = randomCookie

  val nonExistingHeaderName = randomStr
  val header = randomHeader
  val anotherHeader = randomHeader
  val yetAnotherHeader = randomHeader
  val andAnotherHeader = randomHeader

  val content = randomStr
  val anotherContent = randomStr

  val binaryContent = Array[Byte](1, 1, 1, 1)
  val anotherBinaryContent = Array[Byte](2, 2, 2, 2)

  private def randomHeader = randomStr -> randomStr
  private def randomCookie = HttpCookie(randomStr, randomStr)
}

object HttpResponseFactory {

  def aResponseWithNoCookies = aResponseWithCookies()
  def aResponseWithCookies(cookies: HttpCookie*) =
    HttpResponse(headers = immutable.Seq( cookies.map( `Set-Cookie`(_) ):_* ) )

  def aResponseWithNoHeaders = aResponseWithHeaders()
  def aResponseWithHeaders(headers: (String, String)*) = HttpResponse(headers = immutable.Seq( headers.map{ case (k, v) => RawHeader(k, v) }:_* ) )

  def aResponseWith(body: String) = HttpResponse(entity = body)
  def aResponseWith(binaryBody: Array[Byte]) = HttpResponse(entity = binaryBody)
}