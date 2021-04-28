package com.wix.e2e.http

import com.wix.e2e.http.config.Config.DefaultTimeout

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

package object utils {

  def awaitFor[T](future: Future[T]) (implicit atMost: Duration = DefaultTimeout): T =
    Await.result(future, atMost)

  def waitFor[T](future: Future[T]) (implicit atMost: Duration = DefaultTimeout): T =
    awaitFor(future)
}
