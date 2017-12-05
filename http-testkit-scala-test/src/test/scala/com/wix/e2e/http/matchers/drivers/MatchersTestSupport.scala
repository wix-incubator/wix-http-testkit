package com.wix.e2e.http.matchers.drivers

import org.scalatest.matchers.Matcher

//import org.specs2.matcher.{Matcher, MustThrownExpectationsCreation}

trait MatchersTestSupport { //self: MustThrownExpectationsCreation =>
  def failureMessageFor[T](matcher: Matcher[T], matchedOn: T): String =
    matcher.apply( matchedOn ).failureMessage
}
