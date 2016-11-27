package com.wix.hoopoe.http.server.builders

import com.wix.hoopoe.http.server.WebServerFactory.RequestHandler
import com.wix.hoopoe.http.server.internals.{MockAkkaHttpWebServer, StubAkkaHttpMockWebServer}
import com.wix.hoopoe.http.server.{MockWebServer, StubWebServer}

class StubWebServerBuilder(handlers: Seq[RequestHandler], port: Option[Int]) {

  def onPort(port: Int) = new StubWebServerBuilder(handlers, port = Option(port))
  def addHandlers(handler: RequestHandler, handlers: RequestHandler*) = new StubWebServerBuilder(handlers = this.handlers ++ (handler +: handlers), port)
  def addHandler(handler: RequestHandler) = addHandlers(handler)

  def build: StubWebServer = new StubAkkaHttpMockWebServer(handlers, port)
}

class MockWebServerBuilder(handlers: Seq[RequestHandler], port: Option[Int]) {

  def onPort(port: Int) = new MockWebServerBuilder(handlers = handlers, Option(port))

  def build: MockWebServer = new MockAkkaHttpWebServer(handlers, port)
}
