package com.wix.e2e.http.matchers

import com.wix.e2e.http.{HttpRequest, RequestHandler, RequestMatcher}

object Matchers {

  implicit class Matchers(val handler: RequestHandler) extends AnyVal {

    def withPathMatcher(path: String): RequestHandler =
      handlerWithMatcher(pathMatcher(path))

    def withQueryParamMatcher(param: (String, String), params: (String, String)*): RequestHandler =
      handlerWithMatcher(queryParamMatcher(param +: params))

    private def handlerWithMatcher(matcher: RequestMatcher): RequestHandler =
      { case rq: HttpRequest if matcher(rq) => handler(rq) }
  }
}
