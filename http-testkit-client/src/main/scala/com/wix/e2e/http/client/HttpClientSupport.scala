package com.wix.e2e.http.client

import akka.http.scaladsl.client.RequestBuilding.{Delete, Get, Head, Options, Patch, Post, Put, RequestBuilder}
import akka.http.scaladsl.model.HttpMethods.TRACE
import com.wix.e2e.http.client.extractors.HttpMessageExtractors
import com.wix.e2e.http.client.internals.{BlockingRequestManager, NonBlockingRequestManager}
import com.wix.e2e.http.client.transformers.HttpClientTransformers

trait BlockingHttpClientSupport extends HttpClientTransformers with HttpMessageExtractors {
  val get = new BlockingRequestManager(Get())
  val post = new BlockingRequestManager(Post())
  val put = new BlockingRequestManager(Put())
  val patch = new BlockingRequestManager(Patch())
  val delete = new BlockingRequestManager(Delete())
  val options = new BlockingRequestManager(Options())
  val head = new BlockingRequestManager(Head())
  val trace = new BlockingRequestManager(new RequestBuilder(TRACE).apply())
}

trait NonBlockingHttpClientSupport extends HttpClientTransformers with HttpMessageExtractors {
  val get = new NonBlockingRequestManager(Get())
  val post = new NonBlockingRequestManager(Post())
  val put = new NonBlockingRequestManager(Put())
  val patch = new NonBlockingRequestManager(Patch())
  val delete = new NonBlockingRequestManager(Delete())
  val options = new NonBlockingRequestManager(Options())
  val head = new NonBlockingRequestManager(Head())
  val trace = new NonBlockingRequestManager(new RequestBuilder(TRACE).apply())
}

object NonBlockingHttpClientSupport extends NonBlockingHttpClientSupport
object BlockingHttpClientSupport extends BlockingHttpClientSupport
