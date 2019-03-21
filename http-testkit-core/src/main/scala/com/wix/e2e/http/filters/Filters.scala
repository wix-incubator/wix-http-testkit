package com.wix.e2e.http.filters

import com.wix.e2e.http.filters.internal._
import com.wix.e2e.http.{HttpRequest, HttpResponse, RequestFilter, RequestHandler}

trait Filters extends BodyFilters
              with PathFilters
              with QueryParamFilters
              with MethodFilters
              with HeaderFilters
              with Responses {

  val always: RequestFilter = _ => true

  implicit class RequestFilterOps(thisFilter: RequestFilter) {
    def respond(response: HttpResponse): RequestHandler = {
      case rq: HttpRequest if thisFilter(rq) => response
    }

    def and(thatFilter: RequestFilter): RequestFilter =
      (rq: HttpRequest) => thisFilter(rq) && thatFilter(rq)

    def or(thatMatcher: RequestFilter): RequestFilter =
      (rq: HttpRequest) => thisFilter(rq) || thatMatcher(rq)
  }

}

object Filters extends Filters

