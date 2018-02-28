package com.wix.e2e.http

import akka.http.scaladsl.model._
import com.wix.e2e.http.api.Marshaller

package object filters {

  val forAnyRequest: RequestFilter = _ => true

  implicit def `HttpResponse -> RequestHandler`(response: HttpResponse): RequestHandler =
    { case _ => response }

  implicit class MatcherOps(val thisMatcher: RequestFilter) extends AnyVal {

    def respondEntity[T: Manifest](entity: T,
                                   contentType: ContentType.NonBinary = ContentTypes.`application/json`)
                                  (implicit marshaller: Marshaller): RequestHandler = {
      respond(HttpResponse(entity = HttpEntity(contentType, marshaller.marshall(entity))))
    }

    def respond(body: String,
                contentType: ContentType.NonBinary = ContentTypes.`text/plain(UTF-8)`): RequestHandler = {
      respond(HttpResponse(entity = HttpEntity(contentType, body)))
    }

    def respond(response: HttpResponse): RequestHandler =
      respond({ case _ => response }: RequestHandler)

    def respond(handler: RequestHandler): RequestHandler =
      { case rq: HttpRequest if thisMatcher(rq) => handler(rq) }

    def and(thatMatcher: RequestFilter): RequestFilter =
      (rq: HttpRequest) => thisMatcher(rq) && thatMatcher(rq)

    def or(thatMatcher: RequestFilter): RequestFilter =
      (rq: HttpRequest) => thisMatcher(rq) || thatMatcher(rq)
  }

}
