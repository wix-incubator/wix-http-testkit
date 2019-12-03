package com.wix.e2e.http.matchers.drivers

import akka.http.scaladsl.model.headers.HttpCookiePair
import org.scalatest.matchers.should.Matchers._
import org.scalatest.matchers.{MatchResult, Matcher}

trait MatchersTestSupport {
  def failureMessageFor[T](matcher: Matcher[T], matchedOn: T): String =
    matcher.apply( matchedOn ).failureMessage
}

object CommonTestMatchers {

  def cookieWith(value: String): Matcher[HttpCookiePair] = be(value) compose { (_: HttpCookiePair).value }

  case class AlwaysMatcher[T]() extends Matcher[T] {
    def apply(left: T): MatchResult = MatchResult(matches = true, "", "")
  }
}
