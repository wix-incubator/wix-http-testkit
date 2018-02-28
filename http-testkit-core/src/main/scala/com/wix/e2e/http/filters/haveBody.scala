package com.wix.e2e.http.filters

import akka.http.scaladsl.unmarshalling.Unmarshal
import com.wix.e2e.http.RequestFilter
import com.wix.e2e.http.WixHttpTestkitResources._
import com.wix.e2e.http.api.Marshaller

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

object haveBody {

  def apply[T: Manifest](matcher: BodyMatcher[T])(implicit marshaller: Marshaller): RequestFilter = { rq =>
    val str = Await.result(Unmarshal(rq.entity).to[String], 5.seconds)

    Try {
      val entity = marshaller.unmarshall[T](str)
      matcher.matches(entity)
    } getOrElse false
  }
}

trait BodyMatcher[T] {
  def matches(t: T): Boolean
}