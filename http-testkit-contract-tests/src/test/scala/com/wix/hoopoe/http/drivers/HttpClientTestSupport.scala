package com.wix.hoopoe.http.drivers

import akka.http.scaladsl.model.HttpResponse
import com.wix.hoopoe.http.{HttpRequest, RequestHandler}
import com.wixpress.hoopoe.test._

trait HttpClientTestSupport {
  val parameter = randomStrPair
  val header = randomStrPair
  val cookie = randomStrPair
  val path = s"$randomStr/$randomStr"
  val anotherPath = s"$randomStr/$randomStr"
  val someObject = SomeCaseClass(randomStr, randomInt)

  val somePort = randomPort
  val content = randomStr

  def handlerFor(path: String, returnsBody: String): RequestHandler = {
    case r: HttpRequest if r.uri.path.toString.endsWith(path) => HttpResponse(entity = returnsBody)
  }
}

case class SomeCaseClass(s: String, i: Int)