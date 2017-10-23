package com.wix.e2e.http.api

import com.wix.e2e.http.{BaseUri, HttpRequest, RequestHandler}

trait MockWebServer extends BaseWebServer with AdjustableServerBehavior
trait StubWebServer extends BaseWebServer with RequestRecordSupport with AdjustableServerBehavior

trait BaseWebServer {
  def baseUri: BaseUri

  def start(): this.type
  def stop(): this.type
}


trait RequestRecordSupport {
  def recordedRequests: Seq[HttpRequest]
  def clearRecordedRequests(): Unit
}

trait AdjustableServerBehavior {
  def appendAll(handlers: RequestHandler*)
  def replaceWith(handlers: RequestHandler*)
}
