package com.wix.hoopoe.http.client

import akka.http.scaladsl.client.RequestBuilding.{Delete, Get, Head, Options, Patch, Post, Put, RequestBuilder}
import akka.http.scaladsl.model.HttpMethods.TRACE
import com.wix.hoopoe.http.client.internals.BlockingRequestManager
import com.wix.hoopoe.http.client.transformers.HttpClientRequestTransformers

trait SyncHttpClientSupport extends HttpClientRequestTransformers {
  val get = new BlockingRequestManager(Get())
  val post = new BlockingRequestManager(Post())
  val put = new BlockingRequestManager(Put())
  val patch = new BlockingRequestManager(Patch())
  val delete = new BlockingRequestManager(Delete())
  val options = new BlockingRequestManager(Options())
  val head = new BlockingRequestManager(Head())
  val trace = new BlockingRequestManager(new RequestBuilder(TRACE).apply())
}

object SyncHttpClientSupport extends SyncHttpClientSupport