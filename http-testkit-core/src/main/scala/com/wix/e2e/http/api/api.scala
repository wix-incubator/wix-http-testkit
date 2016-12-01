package com.wix.e2e.http.api

import com.wix.e2e.http.{BaseUri, HttpRequest}

trait MockWebServer extends BaseWebServer
trait StubWebServer extends BaseWebServer with RequestRecordSupport

trait BaseWebServer {
  def baseUri: BaseUri

  def start(): this.type
  def stop(): this.type
}


trait RequestRecordSupport {
  def recordedRequests: Seq[HttpRequest]
  def clearRecordedRequests(): Unit
}
