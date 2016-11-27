package com.wix.hoopoe.http.server

import com.wix.hoopoe.http.RequestHandler
import com.wix.hoopoe.http.server.builders.{MockWebServerBuilder, StubWebServerBuilder}

object WebServerFactory {
  def aStubWebServer: StubWebServerBuilder = new StubWebServerBuilder(Seq.empty, None)
  def aMockWebServerWith(handler: RequestHandler, handlers: RequestHandler*): MockWebServerBuilder =
    new MockWebServerBuilder(handler +: handlers, None)
}






