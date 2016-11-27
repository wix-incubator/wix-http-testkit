package com.wix.hoopoe.http.server

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.wix.e2e.BaseUri
import com.wix.hoopoe.http.server.builders.{MockWebServerBuilder, StubWebServerBuilder}

trait MockWebServer extends BaseWebServer
trait StubWebServer extends BaseWebServer with RequestRecordSupport

trait BaseWebServer {
  def baseUri: BaseUri

  def start(): this.type
  def stop(): this.type
}


trait RequestRecordSupport {
  def recordedRequests: List[HttpRequest]
  def clearRecordedRequests(): Unit
}


object WebServerFactory {
  type RequestHandler = PartialFunction[HttpRequest, HttpResponse]

  def aStubWebServer: StubWebServerBuilder = new StubWebServerBuilder(Seq.empty, None)
  def aMockWebServerWith(handler: RequestHandler, handlers: RequestHandler*): MockWebServerBuilder =
    new MockWebServerBuilder(handler +: handlers, None)
}






