package com.wix.e2e.http

import com.wix.e2e.http.filters.CanMatch
import org.scalatest.matchers.Matcher

package object matchers {
  type ResponseMatcher = Matcher[HttpResponse]
  type RequestMatcher = Matcher[HttpRequest]

  implicit def `Matcher -> CanMatch`[T](matcher: Matcher[T]): CanMatch[T] = {
    (t: T) => matcher(t).matches
  }
}
