package com.wix.hoopoe.http.server.builders

import com.wix.hoopoe.http.server.WebServerFactory.RequestHandler
import com.wix.hoopoe.http.server.internals.{MockAkkaHttpWebServer, StubAkkaHttpMockWebServer}
import com.wix.hoopoe.http.server.{MockWebServer, StubWebServer}

case class StubWebServerBuilder(handlers: Seq[RequestHandler], port: Option[Int]) {

  def onPort(port: Int) = copy(port = Option(port))
  def addHandlers(handler: RequestHandler, handlers: RequestHandler*) = copy(handlers = this.handlers ++ (handler +: handlers))
  def addHandler(handler: RequestHandler) = addHandlers(handler)

  def build: StubWebServer = new StubAkkaHttpMockWebServer(handlers, port)
}

case class MockWebServerBuilder(handlers: Seq[RequestHandler], port: Option[Int]) {

  def onPort(port: Int) = copy(port = Option(port))

  def build: MockWebServer = new MockAkkaHttpWebServer(handlers, port)
}
