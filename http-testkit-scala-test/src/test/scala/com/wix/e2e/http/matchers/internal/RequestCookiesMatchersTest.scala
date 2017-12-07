package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.CommonTestMatchers._
import com.wix.e2e.http.matchers.drivers.HttpRequestFactory._
import com.wix.e2e.http.matchers.drivers.{HttpMessageTestSupport, MatchersTestSupport}
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class RequestCookiesMatchersTest extends WordSpec with MatchersTestSupport {

  trait ctx extends HttpMessageTestSupport

  "ResponseCookiesMatchers" should {

    "match if cookiePair with name is found" in new ctx {
      aRequestWithCookies(cookiePair) should receivedCookieWith(cookiePair._1)
    }

    "failure message should describe which cookies are present and which did not match" in new ctx {
      failureMessageFor(receivedCookieWith(cookiePair._1), matchedOn = aRequestWithCookies(anotherCookiePair, yetAnotherCookiePair)) should
        (include(cookiePair._1) and include(anotherCookiePair._1) and include(yetAnotherCookiePair._1))
    }

    "failure message for response withoout cookies will print that the response did not contain any cookies" in new ctx {
      receivedCookieWith(cookiePair._1).apply( aRequestWithNoCookies ).failureMessage should
        include("Request did not contain any Cookie headers.")
    }

    "allow to compose matcher with custom cookiePair matcher" in new ctx {
      aRequestWithCookies(cookiePair) should receivedCookieThat(must = cookieWith(cookiePair._2))
    }
  }
}
