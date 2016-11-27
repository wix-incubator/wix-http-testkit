package com.wix.hoopoe.http.client

import akka.http.scaladsl.client.RequestBuilding.RequestTransformer
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.pattern.AskTimeoutException
import com.wix.hoopoe.http.client.StubWebServerMatchers._
import com.wix.hoopoe.http.client.async._
import com.wix.hoopoe.http.server.StubWebServer
import com.wix.hoopoe.http.server.WebServerFactory.aStubWebServer
import com.wixpress.hoopoe.test._
import org.specs2.matcher.Matcher
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope
import org.specs2.execute.PendingUntilFixed._

import scala.concurrent.Await
import scala.concurrent.duration._


class ASyncHttpClientTest extends SpecWithJUnit {


  trait ctx extends Scope with StubWebServerProvider


  "ASyncHttpClient" should {

    "support generating async get, post, put, patch, delete, options and trace request" in new ctx {
      Seq(get -> "Get", post -> "Post", put -> "Put",
          patch -> "Patch", delete -> "Delete",
          options -> "Options"/*, head -> "Head"*/, trace -> "Trace")
        .foreach { case (method, methodName) =>
          val path = s"$methodName/$randomStr"
          server.clearRecordedRequests()

          Await.result( method(path), Duration.Inf )

          server must receivedRequestWith(methodName, toPath = path)
        }
    }

    "throw timeout if response takes more than default timeout" in {
      val server = aStubWebServer.addHandler( { case _ => Thread.sleep(100); HttpResponse() } )
                                 .build
      server.start()

      Await.result( get("/somePath", withTimeout = 5.millis)(server.baseUri), Duration.Inf) must throwA[AskTimeoutException]

      server.stop()
      ok
    }.pendingUntilFixed("find how to define read timeout")


    "allow to customize request using requrest transformers" in new ctx {
      def withHeaders(headers: (String, String)*): RequestTransformer = _.withHeaders( headers.map { case (k, v) => RawHeader(k, v)}:_* )

      Await.result( get("/somePath", but = withHeaders("h1" -> "v1") ), Duration.Inf)

      server must receivedRequestWith("h1" -> "v1")
    }
  }
}



object StubWebServerMatchers {
  import org.specs2.matcher.Matchers._

  def httpRequestWith(method: String, toPath: String): Matcher[HttpRequest] =
    be_===( toPath ) ^^ { (_: HttpRequest).uri.path.toString().stripPrefix("/") aka "request path"} and
    be_==[String](method).ignoreCase ^^ { (_: HttpRequest).method.name /*aka "method"*/ }

  def httpRequestWith(header: (String, String)): Matcher[HttpRequest] =
    havePair( header ) ^^ { (_: HttpRequest).headers.map( h => h.name -> h.value) aka "request headers" }

  def receivedRequestWith(method: String, toPath: String): Matcher[StubWebServer] = {
    contain(httpRequestWith(method, toPath)).eventually ^^ { (_: StubWebServer).recordedRequests aka "requests" }
  }

  def receivedRequestWith(header: (String, String)): Matcher[StubWebServer] = {
    contain(httpRequestWith(header)).eventually ^^ { (_: StubWebServer).recordedRequests aka "requests" }
  }
}
