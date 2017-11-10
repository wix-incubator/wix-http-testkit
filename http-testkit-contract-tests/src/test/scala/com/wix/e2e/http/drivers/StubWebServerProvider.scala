package com.wix.e2e.http.drivers

import akka.http.scaladsl.model._
import com.wix.e2e.http.BaseUri
import com.wix.e2e.http.server.WebServerFactory.aStubWebServer
import org.specs2.mutable.After

trait StubWebServerProvider extends After {
  val server = aStubWebServer.build
                             .start()

  server.appendAll({
    case HttpRequest(HttpMethods.GET, uri, _, _, _) if uri.path.toString() == "//big-response/512_KiB" =>
      HttpResponse(entity = HttpEntity("." * 1024 * 512))
  })

  def after = server.stop()

  val ClosedPort =  BaseUri(port = 11111)

  lazy implicit val baseUri: BaseUri = server.baseUri
}



//object StubWebServerMatchers {
//  import org.specs2.matcher.Matchers._
//
//  def httpRequestWith(method: String, toPath: String): Matcher[HttpRequest] =
//    be_===( toPath ) ^^ { (_: HttpRequest).uri.path.toString().stripPrefix("/") aka "request path"} and
//      be_==[String](method).ignoreCase ^^ { (_: HttpRequest).method.name /*aka "method"*/ }
//
//  def httpRequestWith(header: (String, String)): Matcher[HttpRequest] =
//    havePair( header ) ^^ { (_: HttpRequest).headers.map( h => h.name -> h.value) aka "request headers" }
//
//  def receivedRequestWith(method: String, toPath: String): Matcher[StubWebServer] = {
//    contain(httpRequestWith(method, toPath)).eventually ^^ { (_: StubWebServer).recordedRequests aka "requests" }
//  }
//
//  def receivedRequestWith(header: (String, String)): Matcher[StubWebServer] = {
//    contain(httpRequestWith(header)).eventually ^^ { (_: StubWebServer).recordedRequests aka "requests" }
//  }
//}
