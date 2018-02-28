package com.wix.e2e.http

import org.scalatest.matchers.Matcher

package object matchers {
  type ResponseMatcher = Matcher[HttpResponse]
  type RequestMatcher = Matcher[HttpRequest]

  implicit def `Matcher -> BodyMatcher`[T](matcher: Matcher[T]): BodyMatcher[T] = {
    (t: T) => matcher(t).matches
  }
}
