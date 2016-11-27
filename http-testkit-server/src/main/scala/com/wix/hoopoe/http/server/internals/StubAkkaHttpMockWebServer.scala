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
    requests.toList
  }

  def clearRecordedRequests() = this.synchronized {
    requests.clear()
  }


  private val SuccessfulHandler: RequestHandler = { case _ => HttpResponse(status = StatusCodes.OK) }
  private val MockServerHandlers = (handlers :+ SuccessfulHandler).reduce(_ orElse _)
  private val RequestRecorderHandler: RequestHandler = { case r =>
    this.synchronized {
      requests.append(r)
    }
    MockServerHandlers.apply(r)
  }

  protected val serverBehavior = RequestRecorderHandler
}

class MockAkkaHttpWebServer(handlers: Seq[RequestHandler], specificPort: Option[Int]) extends AkkaHttpMockWebServer(specificPort) with MockWebServer {

  private val NotFoundHandler: RequestHandler = { case _ => HttpResponse(status = StatusCodes.NotFound) }
  protected def serverBehavior: RequestHandler = (handlers :+ NotFoundHandler).reduce(_ orElse _)
}
