package com.wix.hoopoe.http.server

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

package object utils {
  def awaitFor[T](future: Future[T]) (implicit atMost: Duration = 5.seconds): T =
    Await.result(future, atMost)

  def waitFor[T](future: Future[T]) (implicit atMost: Duration = 5.seconds): T =
    awaitFor(future)
}
