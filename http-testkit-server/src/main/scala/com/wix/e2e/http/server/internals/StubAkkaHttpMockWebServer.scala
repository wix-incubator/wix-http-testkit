package com.wix.e2e.http.server.internals

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import com.wix.e2e.http._
import com.wix.e2e.http.api.{AdjustableServerBehavior, MockWebServer, StubWebServer}

import scala.collection.mutable.ListBuffer

class StubAkkaHttpMockWebServer(initialHandlers: Seq[RequestHandler], specificPort: Option[Int])
  extends AkkaHttpMockWebServer(specificPort, initialHandlers)
  with StubWebServer {


  def recordedRequests: Seq[HttpRequest] = this.synchronized {
    requests.toSeq
  }

  def clearRecordedRequests() = this.synchronized {
    requests.clear()
  }

  private val requests = ListBuffer.empty[HttpRequest]

  private val SuccessfulHandler: RequestHandler = { case _ => HttpResponse(status = StatusCodes.OK) }
  private def StubServerHandlers = (currentHandlers :+ SuccessfulHandler).reduce(_ orElse _)
  private val RequestRecorderHandler: RequestHandler = { case r =>
    this.synchronized {
      requests.append(r)
    }
    StubServerHandlers.apply(r)
  }

  protected val serverBehavior = RequestRecorderHandler
}

class MockAkkaHttpWebServer(initialHandlers: Seq[RequestHandler], specificPort: Option[Int])
  extends AkkaHttpMockWebServer(specificPort, initialHandlers)
  with MockWebServer {

  private val NotFoundHandler: RequestHandler = { case _ => HttpResponse(status = StatusCodes.NotFound) }
  private def MockServerHandlers = (currentHandlers :+ NotFoundHandler).reduce(_ orElse _)
  private val AdjustableHandler: RequestHandler = { case r =>
    MockServerHandlers.apply(r)
  }

  protected val serverBehavior = AdjustableHandler
}

trait AdjustableServerBehaviorSupport extends AdjustableServerBehavior {
  private val localHandlers: ListBuffer[RequestHandler] = ListBuffer(initialHandlers:_*)

  def initialHandlers: Seq[RequestHandler]

  def currentHandlers: Seq[RequestHandler] = this.synchronized {
    localHandlers.toSeq
  }

  def appendAll(handlers: RequestHandler*) = this.synchronized {
    localHandlers.appendAll(handlers)
  }

  def replaceWith(handlers: RequestHandler*) = this.synchronized {
    localHandlers.clear()
    appendAll(handlers:_*)
  }
}
