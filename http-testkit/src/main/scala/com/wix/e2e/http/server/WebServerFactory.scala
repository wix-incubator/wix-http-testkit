package com.wix.e2e.http.server

import com.wix.e2e.http.RequestHandler
import com.wix.e2e.http.exceptions.MisconfiguredMockServerException
import com.wix.e2e.http.server.builders.{MockWebServerBuilder, StubWebServerBuilder}

object WebServerFactory {
  def aStubWebServer: StubWebServerBuilder = new StubWebServerBuilder(Seq.empty, None)
  def aMockWebServerWith(handler: RequestHandler, handlers: RequestHandler*): MockWebServerBuilder = aMockWebServerWith(handler +: handlers)
  def aMockWebServerWith(handlers: Seq[RequestHandler]): MockWebServerBuilder =
    handlers match {
      case h if h.isEmpty => throw new MisconfiguredMockServerException
      case h => new MockWebServerBuilder(h, None)
    }
}
