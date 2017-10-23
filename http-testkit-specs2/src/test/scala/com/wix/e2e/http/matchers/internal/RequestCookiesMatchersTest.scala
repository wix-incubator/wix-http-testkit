package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.headers.HttpCookiePair
import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.HttpRequestFactory._
import com.wix.e2e.http.matchers.drivers.MatchersTestSupport
import com.wix.test.random._
import org.specs2.matcher.Matcher
import org.specs2.matcher.Matchers._
import org.specs2.mutable.Spec
import org.specs2.specification.Scope


class RequestCookiesMatchersTest extends Spec with MatchersTestSupport {

  trait ctx extends Scope {
    val cookie = randomStr -> randomStr
    val anotherCookie = randomStr -> randomStr
    val yetAnotherCookie = randomStr -> randomStr
  }

  def cookieWith(value: String): Matcher[HttpCookiePair] = be_===(value) ^^ { (_: HttpCookiePair).value aka "cookie value" }

  "ResponseCookiesMatchers" should {

    "match if cookie with name is found" in new ctx {
      aRequestWithCookies(cookie) must receivedCookieWith(cookie._1)
    }

    "failure message should describe which cookies are present and which did not match" in new ctx {
      failureMessageFor(receivedCookieWith(cookie._1), matchedOn = aRequestWithCookies(anotherCookie, yetAnotherCookie)) must
        contain(cookie._1) and contain(anotherCookie._1) and contain(yetAnotherCookie._1)
    }

    "failure message for response withoout cookies will print that the response did not contain any cookies" in new ctx {
      receivedCookieWith(cookie._1).apply( aRequestWithNoCookies ).message must
        contain("Request did not contain any Cookie headers.")
    }

    "allow to compose matcher with custom cookie matcher" in new ctx {
      aRequestWithCookies(cookie) must receivedCookieThat(must = cookieWith(cookie._2))
    }
  }
}
