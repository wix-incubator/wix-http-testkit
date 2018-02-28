package com.wix.e2e.http.filters

import akka.http.scaladsl.model._
import com.wix.e2e.http.HttpResponse
import com.wix.e2e.http.api.Marshaller

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