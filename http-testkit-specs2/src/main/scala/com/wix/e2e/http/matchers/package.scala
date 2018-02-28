package com.wix.e2e.http

import com.wix.e2e.http.filters.BodyMatcher
import org.specs2.matcher.MustExpectations._
import org.specs2.matcher.{MatchSuccess, Matcher}

package object matchers {
  type ResponseMatcher = Matcher[HttpResponse]
  type RequestMatcher = Matcher[HttpRequest]

  implicit def `Matcher -> BodyMatcher`[T](matcher: Matcher[T]): BodyMatcher[T] = {
    (t: T) => matcher(createExpectable(t)) match {
      case _: MatchSuccess[T] => true
      case _ => false
    }
  }
}
