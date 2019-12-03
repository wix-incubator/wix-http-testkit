package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.CommonTestMatchers._
import com.wix.e2e.http.matchers.drivers.HttpRequestFactory._
import com.wix.e2e.http.matchers.drivers.{HttpMessageTestSupport, MatchersTestSupport}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class RequestCookiesMatchersTest extends AnyWordSpec with MatchersTestSupport {

  trait ctx extends HttpMessageTestSupport

  "ResponseCookiesMatchers" should {

    "match if cookiePair with name is found" in new ctx {
      aRequestWithCookies(cookiePair) should receivedCookieWith(cookiePair._1)
    }

    "failure message should describe which cookies are present and which did not match" in new ctx {
      failureMessageFor( receivedCookieWith(cookiePair._1), matchedOn = aRequestWithCookies(anotherCookiePair, yetAnotherCookiePair)) should
        include(s"Could not find cookie that matches for request contained cookies with names: ['${anotherCookiePair._1}', '${yetAnotherCookiePair._1}'")
      failureMessageFor( not( receivedCookieThat(be(cookiePair._1)) ), matchedOn = aRequestWithCookies(cookiePair, anotherCookiePair)) shouldBe
        s"Request contained a cookie that matched, request has the following cookies: ['${cookiePair._1}', '${anotherCookiePair._1}'"
    }

    "failure message for response withoout cookies will print that the response did not contain any cookies" in new ctx {
      failureMessageFor( receivedCookieWith(cookiePair._1), matchedOn = aRequestWithNoCookies) shouldBe
        "Request did not contain any Cookie headers."
      failureMessageFor( not( receivedCookieWith(cookiePair._1) ), matchedOn = aRequestWithNoCookies) shouldBe
        "Request did not contain any Cookie headers."
    }

    "allow to compose matcher with custom cookiePair matcher" in new ctx {
      aRequestWithCookies(cookiePair) should receivedCookieThat(must = cookieWith(cookiePair._2))
    }
  }
}
