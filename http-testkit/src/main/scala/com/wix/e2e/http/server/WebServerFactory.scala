package com.wix.e2e.http.server

import com.wix.e2e.http.RequestHandler
import com.wix.e2e.http.server.builders.{MockWebServerBuilder, StubWebServerBuilder}

object WebServerFactory {
  def aStubWebServer: StubWebServerBuilder = new StubWebServerBuilder(Seq.empty, None)

  def aMockWebServer: MockWebServerBuilder = aMockWebServerWith(Seq.empty)
  def aMockWebServerWith(handler: RequestHandler, handlers: RequestHandler*): MockWebServerBuilder = aMockWebServerWith(handler +: handlers)
  def aMockWebServerWith(handlers: Seq[RequestHandler]): MockWebServerBuilder = new MockWebServerBuilder(handlers, None)
}
