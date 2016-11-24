package com.wix.hoopoe.http.server.internals

import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import com.wix.hoopoe.http.server.WebServerFactory.RequestHandler
import com.wix.hoopoe.http.server.{MockWebServer, StubWebServer}

import scala.collection.mutable.ListBuffer

class StubAkkaHttpMockWebServer(handlers: Seq[RequestHandler], specificPort: Option[Int])
  extends AkkaHttpMockWebServer(specificPort)
  with StubWebServer {

  private val requests = ListBuffer.empty[HttpRequest]

  def recordedRequests = this.synchronized {
    requests
  }

  def clearRecordedRequests() = this.synchronized {
    requests.clear()
  }


  private val SuccessfulHandler: RequestHandler = { case _: HttpRequest => HttpResponse(status = StatusCodes.OK) }
  private val RequestRecorderHandler: RequestHandler = { case r: HttpRequest =>
    this.synchronized {
      requests.append(r)
    }
    (handlers :+ SuccessfulHandler).reduce(_ orElse _).apply(r)
  }

  protected val serverBehavior = RequestRecorderHandler
}

class MockAkkaHttpWebServer(handlers: Seq[RequestHandler], specificPort: Option[Int]) extends AkkaHttpMockWebServer(specificPort) with MockWebServer {

  private val NotFoundHandler: RequestHandler = { case _: HttpRequest => HttpResponse(status = StatusCodes.NotFound) }
  protected def serverBehavior: RequestHandler = (handlers :+ NotFoundHandler).reduce(_ orElse _)
}
