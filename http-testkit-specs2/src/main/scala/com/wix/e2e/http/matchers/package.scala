package com.wix.e2e.http

import com.wix.e2e.http.filters.CanMatch
import org.specs2.matcher.MustExpectations._
import org.specs2.matcher.{MatchSuccess, Matcher}

package object matchers {
  type ResponseMatcher = Matcher[HttpResponse]
  type RequestMatcher = Matcher[HttpRequest]

  implicit def `Matcher -> CanMatch`[T](matcher: Matcher[T]): CanMatch[T] = {
    (t: T) => matcher(createExpectable(t)) match {
      case _: MatchSuccess[T] => true
      case _ => false
    }
  }
}
