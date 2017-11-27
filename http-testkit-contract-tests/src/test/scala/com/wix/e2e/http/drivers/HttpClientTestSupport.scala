package com.wix.e2e.http.drivers

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import com.wix.e2e.http.info.HttpTestkitVersion
import com.wix.e2e.http.matchers.{RequestMatcher, ResponseMatcher}
import com.wix.e2e.http.{HttpRequest, RequestHandler}
import com.wix.test.random._

trait HttpClientTestSupport {
  val parameter = randomStrPair
  val header = randomStrPair
  val formData = randomStrPair
  val userAgent = randomStr
  val cookie = randomStrPair
  val path = s"$randomStr/$randomStr"
  val anotherPath = s"$randomStr/$randomStr"
  val someObject = SomeCaseClass(randomStr, randomInt)

  val somePort = randomPort
  val content = randomStr
  val anotherContent = randomStr

  def handlerFor(path: String, returnsBody: String): RequestHandler = {
    case r: HttpRequest if r.uri.path.toString.endsWith(path) => HttpResponse(entity = returnsBody)
  }

  val bigResponse = 1024 * 1024

  def bigResponseWith(size: Int): RequestHandler = {
    case HttpRequest(GET, uri, _, _, _) if uri.path.toString().contains("big-response") =>
      HttpResponse(entity = HttpEntity(randomStrWith(size)))
  }
}

case class SomeCaseClass(s: String, i: Int)

object HttpClientMatchers {
  import com.wix.e2e.http.matchers.RequestMatchers._

  def haveClientHttpTestkitUserAgentWithLibraryVersion: RequestMatcher =
    haveAnyHeadersOf("User-Agent" -> s"client-http-testkit/$HttpTestkitVersion")
}

object HttpServerMatchers {
  import com.wix.e2e.http.matchers.ResponseMatchers._

  def haveServerHttpTestkitHeaderWithLibraryVersion: ResponseMatcher =
    haveAnyHeadersOf("Server" -> s"server-http-testkit/$HttpTestkitVersion")
}