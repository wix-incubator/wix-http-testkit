package com.wix.e2e.http.drivers

import java.io.DataOutputStream
import java.net.{HttpURLConnection, URL}

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import com.wix.e2e.http.client.extractors.HttpMessageExtractors._
import com.wix.e2e.http.info.HttpTestkitVersion
import com.wix.e2e.http.matchers.{RequestMatcher, ResponseMatcher}
import com.wix.e2e.http.{BaseUri, HttpRequest, RequestHandler}
import com.wix.test.random._

import scala.collection.mutable.ListBuffer

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

  val requestData = ListBuffer.empty[String]

  def handlerFor(path: String, returnsBody: String): RequestHandler = {
    case r: HttpRequest if r.uri.path.toString.endsWith(path) => HttpResponse(entity = returnsBody)
  }

  def unmarshallingAndStoringHandlerFor(path: String, storeTo: ListBuffer[String]): RequestHandler = {
      case r: HttpRequest if r.uri.path.toString.endsWith(path) =>
        storeTo.append( r.extractAsString )
        HttpResponse()
    }

  val bigResponse = 1024 * 1024

  def bigResponseWith(size: Int): RequestHandler = {
    case HttpRequest(GET, uri, _, _, _) if uri.path.toString().contains("big-response") =>
      HttpResponse(entity = HttpEntity(randomStrWith(size)))
  }

  def issueChunkedPostRequestWith(content: String, toPath: String)(implicit baseUri: BaseUri) = {
    val serverUrl = new URL(s"http://localhost:${baseUri.port}/$toPath")
    val conn = serverUrl.openConnection.asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("POST")
    conn.setRequestProperty("Content-Type", "text/plain")
    conn.setChunkedStreamingMode(0)
    conn.setDoOutput(true)
    conn.setDoInput(true)
    conn.setUseCaches(false)
    conn.connect()

    val out = new DataOutputStream(conn.getOutputStream)
    out.writeBytes(content)
    out.flush()
    out.close()
    conn.disconnect()
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