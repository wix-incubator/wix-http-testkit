package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.CommonTestMatchers._
import com.wix.e2e.http.matchers.drivers.HttpRequestFactory._
import com.wix.e2e.http.matchers.drivers.{HttpMessageTestSupport, MatchersTestSupport}
import org.specs2.matcher.Matchers._
import org.specs2.mutable.Spec
import org.specs2.specification.Scope


class RequestCookiesMatchersTest extends Spec with MatchersTestSupport {

  trait ctx extends Scope with HttpMessageTestSupport

  "ResponseCookiesMatchers" should {

    "match if cookie with name is found" in new ctx {
      aRequestWithCookies(cookiePair) must receivedCookieWith(cookiePair._1)
    }

    "failure message should describe which cookies are present and which did not match" in new ctx {
      failureMessageFor(receivedCookieWith(cookiePair._1), matchedOn = aRequestWithCookies(anotherCookiePair, yetAnotherCookiePair)) must
        contain(cookiePair._1) and contain(anotherCookiePair._1) and contain(yetAnotherCookiePair._1)
    }

    "failure message for response withoout cookies will print that the response did not contain any cookies" in new ctx {
      receivedCookieWith(cookiePair._1).apply( aRequestWithNoCookies ).message must
        contain("Request did not contain any Cookie headers.")
    }

    "allow to compose matcher with custom cookie matcher" in new ctx {
      aRequestWithCookies(cookiePair) must receivedCookieThat(must = cookieWith(cookiePair._2))
    }
  }
}
