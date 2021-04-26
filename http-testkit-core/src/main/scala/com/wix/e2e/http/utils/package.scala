package com.wix.e2e.http

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

package object utils {

  val DefaultTimeout: FiniteDuration = Option(System.getProperty("com.wix.e2e.http.util.DefaultTimeout"))
    .flatMap(timeoutStr => Try { FiniteDuration(timeoutStr.toLong, SECONDS) }.toOption)
    .getOrElse(5.seconds)

  def awaitFor[T](future: Future[T]) (implicit atMost: Duration = DefaultTimeout): T =
    Await.result(future, atMost)

  def waitFor[T](future: Future[T]) (implicit atMost: Duration = DefaultTimeout): T =
    awaitFor(future)
}
