package com.wix.e2e.http.filters

import akka.http.scaladsl.unmarshalling.Unmarshal
import com.wix.e2e.http.RequestFilter
import com.wix.e2e.http.WixHttpTestkitResources._
import com.wix.e2e.http.api.Marshaller

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

trait BodyFilters {

  def forBody[T: Manifest](matcher: BodyMatcher[T])(implicit marshaller: Marshaller): RequestFilter = { rq =>
    val str = Await.result(Unmarshal(rq.entity).to[String], 5.seconds)

    Try(marshaller.unmarshall[T](str)) map matcher.matches getOrElse false
  }
}

trait BodyMatcher[T] {
  def matches(t: T): Boolean
}