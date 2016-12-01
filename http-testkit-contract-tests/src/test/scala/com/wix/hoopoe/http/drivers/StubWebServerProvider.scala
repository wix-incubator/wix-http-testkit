package com.wix.hoopoe.http.drivers

import com.wix.hoopoe.http.BaseUri
import com.wix.hoopoe.http.server.WebServerFactory.aStubWebServer
import org.specs2.mutable.After

trait StubWebServerProvider extends After {
  val server = aStubWebServer.build
                             .start()

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
