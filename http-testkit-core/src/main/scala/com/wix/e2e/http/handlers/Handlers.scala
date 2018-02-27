package com.wix.e2e.http.handlers

import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpEntity, HttpResponse}
import com.wix.e2e.http.{HttpRequest, RequestHandler}
import com.wix.e2e.http.api.Marshaller

object Handlers {

  def entityHandler[T : Manifest](entity: T)(implicit marshaller: Marshaller): RequestHandler = {
    stringHandler(marshaller.marshall(entity), contentType = ContentTypes.`application/json`)
  }

  def stringHandler(entity: String,
                    contentType: ContentType.NonBinary = ContentTypes.`text/plain(UTF-8)`): RequestHandler = {
    case (rq: HttpRequest) => HttpResponse(entity = HttpEntity(contentType, entity))
  }
}
