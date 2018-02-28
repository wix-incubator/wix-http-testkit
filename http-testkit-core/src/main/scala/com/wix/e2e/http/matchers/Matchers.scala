package com.wix.e2e.http.matchers

import akka.http.scaladsl.model._
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.{HttpRequest, RequestHandler, RequestMatcher}

object Matchers {

  val forAnyRequest: RequestMatcher = _ => true

  implicit def `HttpResponse -> RequestHandler`(response: HttpResponse): RequestHandler =
    { case _ => response }

  implicit class MatcherOps(val thisMatcher: RequestMatcher) extends AnyVal {

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

    def and(thatMatcher: RequestMatcher): RequestMatcher =
      (rq: HttpRequest) => thisMatcher(rq) && thatMatcher(rq)

    def or(thatMatcher: RequestMatcher): RequestMatcher =
      (rq: HttpRequest) => thisMatcher(rq) || thatMatcher(rq)
  }

  trait Responses {
    implicit val stringWriter = new Writer[String] {
      override def write(t: String): String = t
      override def contentType = ContentTypes.`text/plain(UTF-8)`
    }

    implicit def `Marshaller -> Writer[T]`[T: Manifest](implicit marshaller: Marshaller): Writer[T] = {
      (t: T) => marshaller.marshall(t)
    }

    def ok[T: Writer](body: T = ""): HttpResponse = response(body, StatusCodes.OK)
    def created[T: Writer](body: T = ""): HttpResponse = response(body, StatusCodes.Created)
    def accepted[T: Writer](body: T = ""): HttpResponse = response(body, StatusCodes.Accepted)
    def noContent[T: Writer]: HttpResponse = response("", StatusCodes.NoContent)

    def badRequest[T: Writer](body: T = ""): HttpResponse = response(body, StatusCodes.BadRequest)
    def unauthorized[T: Writer](body: T = ""): HttpResponse = response(body, StatusCodes.Unauthorized)
    def forbidden[T: Writer](body: T = ""): HttpResponse = response(body, StatusCodes.Forbidden)
    def notFound[T: Writer]: HttpResponse = response("", StatusCodes.NotFound)
    def conflict[T: Writer](body: T = ""): HttpResponse = response(body, StatusCodes.Conflict)

    def internalServerError[T: Writer](body: T = ""): HttpResponse = response(body, StatusCodes.InternalServerError)

    def response[T](body: T, status: StatusCode)(implicit writer: Writer[T]) =
      HttpResponse(status = status, entity = HttpEntity(writer.contentType, writer.write(body)))
  }

  object Responses extends Responses

  trait Writer[T] {
    def write(t: T): String
    def contentType: ContentType.NonBinary = ContentTypes.`application/json`
  }

}
