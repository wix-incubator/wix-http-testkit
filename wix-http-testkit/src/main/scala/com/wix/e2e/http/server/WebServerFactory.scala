package com.wix.e2e.http.server

import com.wix.e2e.http.RequestHandler
import com.wix.e2e.http.server.builders.{MockWebServerBuilder, StubWebServerBuilder}

object WebServerFactory {
  def aStubWebServer: StubWebServerBuilder = new StubWebServerBuilder(Seq.empty, None)
  def aMockWebServerWith(handler: RequestHandler, handlers: RequestHandler*): MockWebServerBuilder =
    new MockWebServerBuilder(handler +: handlers, None)
}






