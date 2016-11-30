package com.wix.hoopoe.http

import java.util.concurrent.TimeoutException

import akka.pattern.after

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

package object utils {
  def awaitFor[T](future: Future[T]) (implicit atMost: Duration = 5.seconds): T =
    Await.result(future, atMost)

  def waitFor[T](future: Future[T]) (implicit atMost: Duration = 5.seconds): T =
    awaitFor(future)

  implicit class FutureOps[T](private val f: Future[T]) extends AnyVal {
    def withTimeoutOf(duration: FiniteDuration): Future[T] =
      Future.firstCompletedOf(Seq( schedule(timeout = duration), f))

    private def schedule(timeout: FiniteDuration): Future[T] =
      after(timeout, WixHttpTestkitResources.system.scheduler)( Future.failed(new TimeoutException ) )
  }
}
