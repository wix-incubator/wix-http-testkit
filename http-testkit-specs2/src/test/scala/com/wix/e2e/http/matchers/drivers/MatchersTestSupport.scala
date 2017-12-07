package com.wix.e2e.http.matchers.drivers

import akka.http.scaladsl.model.headers.HttpCookiePair
import org.specs2.matcher.Matchers._
import org.specs2.matcher.{Matcher, MustThrownExpectationsCreation}

trait MatchersTestSupport { self: MustThrownExpectationsCreation =>
  def failureMessageFor[T](matcher: Matcher[T], matchedOn: T): String =
    matcher.apply( createMustExpectable(matchedOn) ).message
}

object CommonTestMatchers {
  def cookieWith(value: String): Matcher[HttpCookiePair] = be_===(value) ^^ { (_: HttpCookiePair).value aka "cookie value" }
}
