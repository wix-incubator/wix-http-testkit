package com.wix.e2e.http.matchers

import com.wix.e2e.http.{HttpRequest, RequestHandler, RequestMatcher}

object Matchers {

  implicit class HandlerOps(val handler: RequestHandler) extends AnyVal {

    def matchWith(matcher: RequestMatcher): RequestHandler =
      { case rq: HttpRequest if matcher(rq) => handler(rq) }

    def &&(matcher: RequestMatcher): RequestHandler = matchWith(matcher)
  }

  implicit class MatcherOps(val thisMatcher: RequestMatcher) extends AnyVal {

    def handleWith(handler: RequestHandler): RequestHandler =
      handler matchWith thisMatcher

    def &&(thatMatcher: RequestMatcher): RequestMatcher =
      (rq: HttpRequest) => thisMatcher(rq) && thatMatcher(rq)

    def ||(thatMatcher: RequestMatcher): RequestMatcher =
      (rq: HttpRequest) => thisMatcher(rq) || thatMatcher(rq)
  }

}
