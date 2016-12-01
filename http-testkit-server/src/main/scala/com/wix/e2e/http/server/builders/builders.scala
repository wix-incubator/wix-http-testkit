package com.wix.e2e.http.server.builders

import com.wix.e2e.http.RequestHandler
import com.wix.e2e.http.api.{MockWebServer, StubWebServer}
import com.wix.e2e.http.server.internals.{MockAkkaHttpWebServer, StubAkkaHttpMockWebServer}

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
