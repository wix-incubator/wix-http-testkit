package com.wix.e2e.http.matchers.drivers

import org.scalatest.matchers.Matcher

trait MatchersTestSupport {
  def failureMessageFor[T](matcher: Matcher[T], matchedOn: T): String =
    matcher.apply( matchedOn ).failureMessage
}
