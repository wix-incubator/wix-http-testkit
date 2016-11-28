package com.wix.hoopoe.http

import org.specs2.matcher.Matcher

package object matchers {
  type ResponseMatcher = Matcher[HttpResponse]
}
