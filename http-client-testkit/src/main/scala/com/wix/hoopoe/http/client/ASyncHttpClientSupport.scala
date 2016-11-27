package com.wix.hoopoe.http.client

import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.HttpMethods.TRACE
import com.wix.hoopoe.http.client.internals.NonBlockingRequestManager
import com.wix.hoopoe.http.client.transformers.HttpClientRequestTransformers

trait ASyncHttpClientSupport extends HttpClientRequestTransformers {
  val get = new NonBlockingRequestManager(Get())
  val post = new NonBlockingRequestManager(Post())
  val put = new NonBlockingRequestManager(Put())
  val patch = new NonBlockingRequestManager(Patch())
  val delete = new NonBlockingRequestManager(Delete())
  val options = new NonBlockingRequestManager(Options())
  val head = new NonBlockingRequestManager(Head())
  val trace = new NonBlockingRequestManager(new RequestBuilder(TRACE).apply())
}

object ASyncHttpClientSupport extends ASyncHttpClientSupport