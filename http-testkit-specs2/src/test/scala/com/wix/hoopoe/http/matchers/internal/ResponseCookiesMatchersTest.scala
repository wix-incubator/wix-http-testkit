package com.wix.hoopoe.http.matchers.internal

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers._
import com.wix.hoopoe.http.matchers.ResponseMatchers._
import com.wix.hoopoe.http.matchers.internal.HttpResponseFactory._
import com.wixpress.hoopoe.test._
import org.specs2.matcher.{Matcher, MustThrownExpectationsCreation}
import org.specs2.matcher.Matchers._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

import scala.collection.immutable


class ResponseCookiesMatchersTest extends SpecWithJUnit with MatchersTestSupport {

  trait ctx extends Scope with HttpResponseTestSupport


  "ResponseCookiesMatchers" should {

    "match if cookie with name is found" in new ctx {
      aResponseWithCookies(cookie) must receivedCookieWith(cookie.name)
    }

    "failure message should describe which cookies are present and which did not match" in new ctx {
      failureMessageFor(receivedCookieWith(cookie.name), matchedOn = aResponseWithCookies(anotherCookie, yetAnotherCookie)) must
        contain(cookie.name) and contain(anotherCookie.name) and contain(yetAnotherCookie.name)
    }

    "failure message for response withoout cookies will print that the response did not contain any cookies" in new ctx {
      receivedCookieWith(cookie.name).apply( aResponseWithNoCookies ).message must
        contain("Response did not contain any `Set-Cookie` headers.")
    }

    "allow to compose matcher with custom cookie matcher" in new ctx {
      aResponseWithCookies(cookie) must receivedCookieThat(be_===(cookie.value) ^^ { (_: HttpCookie).value aka "cookie value" })
    }
  }
}

trait MatchersTestSupport { self: MustThrownExpectationsCreation =>
  def failureMessageFor[T](matcher: Matcher[T], matchedOn: T): String =
    matcher.apply( createMustExpectable(matchedOn) ).message
}

trait HttpResponseTestSupport {

  val cookie = randomCookie
  val anotherCookie = randomCookie
  val yetAnotherCookie = randomCookie

  val nonExistingHeaderName = randomStr
  val header = randomHeader
  val anotherHeader = randomHeader
  val yetAnotherHeader = randomHeader
  val andAnotherHeader = randomHeader


  private def randomHeader = randomStr -> randomStr
  private def randomCookie = HttpCookie(randomStr, randomStr)
}

object HttpResponseFactory {

  def aResponseWithNoCookies = aResponseWithCookies()
  def aResponseWithCookies(cookies: HttpCookie*) =
    HttpResponse(headers = immutable.Seq( cookies.map( `Set-Cookie`(_) ):_* ) )

  def aResponseWithNoHeaders = aResponseWithHeaders()
  def aResponseWithHeaders(headers: (String, String)*) = HttpResponse(headers = immutable.Seq( headers.map{ case (k, v) => RawHeader(k, v) }:_* ) )
}