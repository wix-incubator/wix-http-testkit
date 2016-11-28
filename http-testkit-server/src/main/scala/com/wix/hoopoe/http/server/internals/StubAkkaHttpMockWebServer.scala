package com.wix.hoopoe.http.server.internals

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import com.wix.hoopoe.http._
import com.wix.hoopoe.http.api.{MockWebServer, StubWebServer}

import scala.collection.mutable.ListBuffer

class StubAkkaHttpMockWebServer(handlers: Seq[RequestHandler], specificPort: Option[Int])
  extends AkkaHttpMockWebServer(specificPort)
  with StubWebServer {


  def recordedRequests = this.synchronized {
    requests
  }

  def clearRecordedRequests() = this.synchronized {
    requests.clear()
  }

  private val requests = ListBuffer.empty[HttpRequest]

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

class MockAkkaHttpWebServer(handlers: Seq[RequestHandler], specificPort: Option[Int])
  extends AkkaHttpMockWebServer(specificPort)
  with MockWebServer {

  private val NotFoundHandler: RequestHandler = { case _ => HttpResponse(status = StatusCodes.NotFound) }
  protected def serverBehavior: RequestHandler = (handlers :+ NotFoundHandler).reduce(_ orElse _)
}
