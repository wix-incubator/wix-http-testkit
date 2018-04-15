package com.wix.e2e.http.examples

import akka.http.scaladsl.model.HttpMethods.{GET, PUT}
import akka.http.scaladsl.model.MediaTypes.`image/png`
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import com.wix.e2e.http.RequestHandler
import com.wix.e2e.http.client.extractors._
import com.wix.e2e.http.server.WebServerFactory.aMockWebServerWith

import scala.collection.concurrent.TrieMap

class MediaServer(port: Int, uploadPath: String, downloadPath: String) {

  private val mockWebServer = aMockWebServerWith( {
    case HttpRequest(PUT, u, headers, entity, _) if u.path.tail == Path(uploadPath) =>
      handleMediaPost(u, headers.toList, entity)

    case HttpRequest(GET, u, headers, _, _) if u.path.tail.toString().startsWith(downloadPath) =>
      handleMediaGet(u, headers.toList)

  } : RequestHandler).onPort(port)
                     .build.start()

  def stop() = mockWebServer.stop()

  private def handleMediaPost(uri: Uri, headers: List[HttpHeader], entity: HttpEntity): HttpResponse = {
    val fileName = headers.find( _.name == "filename").map( _.value ).orElse( uri.query().toMap.get("f") ).get
    val media = entity.extractAsBytes
    files.put(fileName, media)
    HttpResponse()
  }

  private def handleMediaGet(uri: Uri, headers: List[HttpHeader]): HttpResponse = {
    val fileName = uri.path.reverse
                      .head.toString
                      .stripPrefix("/")
    files.get(fileName)
      .map( i => HttpResponse(entity = HttpEntity(`image/png`, i)) )
      .getOrElse( HttpResponse(status = NotFound) )
  }

  private val files = TrieMap.empty[String, Array[Byte]]
}
