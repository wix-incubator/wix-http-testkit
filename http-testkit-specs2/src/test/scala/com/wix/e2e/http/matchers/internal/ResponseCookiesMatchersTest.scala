package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.HttpResponseMatchers._
import com.wix.e2e.http.matchers.drivers.{HttpResponseTestSupport, MatchersTestSupport}
import org.specs2.matcher.Matchers._
import org.specs2.mutable.Spec
import org.specs2.specification.Scope


class ResponseCookiesMatchersTest extends Spec with MatchersTestSupport {

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
      aResponseWithCookies(cookie) must receivedCookieThat(must = cookieWith(cookie.value))
    }
  }
}
