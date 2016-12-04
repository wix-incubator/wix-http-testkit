package com.wix.e2e.http.examples

import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.Uri.Path
import com.wix.e2e.http.RequestHandler
import com.wix.e2e.http.server.WebServerFactory.aMockWebServerWith

class FileServer {

  private val UploadPath = "upload"
  private val DownloadPath = "download"

  private val UploadFileHandler: RequestHandler =
  { case HttpRequest(POST, u, _, entity, _) if u.path.tail == Path(UploadPath) => ??? }

  private val DownloadFileHandler: RequestHandler =
  { case HttpRequest(GET, u, _, _, _) if u.path.tail == Path(DownloadPath) =>
      println( u.path.tail )
      HttpResponse(entity = "yo !!!") }

  private val server = aMockWebServerWith(UploadFileHandler, DownloadFileHandler).build.start()
//  protected def initProbe(uploadPath: String, downloadPath: String): Unit = {
//    embeddedHttpProbe.handlers += {
//
//      case HttpRequest(POST, u, headers, entity, _) if u.path.tail == Path(uploadPath) =>
//        handleMediaPost(u, headers, entity)
//
//      case HttpRequest(PUT, u, headers, entity, _) if u.path.tail == Path(uploadPath) =>
//        handleMediaPost(u, headers, entity)
//
//      case HttpRequest(GET, u, headers, _, _) if u.path.tail.toString().startsWith(downloadPath) =>
//        handleMediaGet(u, headers)
//    }
//  }
//
//  protected def respondInvalidWith(code: Int, message: String) = HttpResponse(status = BadRequest, entity = HttpEntity(ContentTypes.`application/json`, PrivateMediaResponse(message, code).asJsonStr))
//  protected def handleMediaPost(uri: Uri, headers: List[HttpHeader], entity: HttpEntity): HttpResponse
//  protected def handleMediaGet(uri: Uri, headers: List[HttpHeader]): HttpResponse
}


object FileServerApp extends App {

  val fileServer = new FileServer

}