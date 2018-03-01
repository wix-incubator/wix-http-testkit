package com.wix.e2e.http.filters


import akka.http.scaladsl.model._
import com.wix.e2e.http.{RequestFilter, RequestHandler}
import com.wix.e2e.http.api.Marshaller

trait Responses {
  implicit val stringWriter: Writer[String] = new Writer[String] {
    override def write(t: String): String = t
    override def contentType = ContentTypes.`text/plain(UTF-8)`
  }

  implicit def `Marshaller -> Writer[T]`[T: Manifest](implicit marshaller: Marshaller): Writer[T] =
    (t: T) => marshaller.marshall(t)

  implicit def `RequestFilter -> RequestHandler`(filter: RequestFilter) = new `RequestFilter -> RequestHandler`(filter)

}

object Responses extends Responses

class `RequestFilter -> RequestHandler`(val filter: RequestFilter) extends AnyVal {
  import Responses._

  def respondOk[T: Writer](body: T = ""): RequestHandler = respond(body, StatusCodes.OK)
  def respondCreated[T: Writer](body: T = ""): RequestHandler = respond(body, StatusCodes.Created)
  def respondAccepted[T: Writer](body: T = ""): RequestHandler = respond(body, StatusCodes.Accepted)
  def respondNoContent(): RequestHandler = respond("", StatusCodes.NoContent)
  
  def respondBadRequest[T: Writer](body: T = ""): RequestHandler = respond(body, StatusCodes.BadRequest)
  def respondUnauthorized[T: Writer](body: T = ""): RequestHandler = respond(body, StatusCodes.Unauthorized)
  def respondForbidden[T: Writer](body: T = ""): RequestHandler = respond(body, StatusCodes.Forbidden)
  def respondNotFound(): RequestHandler = respond("", StatusCodes.NotFound)
  def respondConflict[T: Writer](body: T = ""): RequestHandler = respond(body, StatusCodes.Conflict)
  
  def respondInternalServerError[T: Writer](body: T = ""): RequestHandler = respond(body, StatusCodes.InternalServerError)

  def respond[T](body: T, status: StatusCode)(implicit writer: Writer[T]): RequestHandler =
    respond(body, status, writer.contentType)

  def respond[T](body: T, status: StatusCode, contentType: ContentType.NonBinary)(implicit writer: Writer[T]): RequestHandler = {
    case rq if filter(rq) => HttpResponse(status = status, entity = HttpEntity(contentType, writer.write(body)))
  }
}

trait Writer[T] {
  def write(t: T): String
  def contentType: ContentType.NonBinary = ContentTypes.`application/json`
}