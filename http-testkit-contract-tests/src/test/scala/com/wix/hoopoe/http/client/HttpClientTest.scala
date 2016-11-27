package com.wix.hoopoe.http.client

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.pattern.AskTimeoutException
import com.wix.hoopoe.http.BaseUri
import com.wix.hoopoe.http.api.StubWebServer
import com.wix.hoopoe.http.client.StubWebServerMatchers._
import com.wix.hoopoe.http.server.WebServerFactory.aStubWebServer
import com.wixpress.hoopoe.test._
import org.specs2.matcher.Matcher
import org.specs2.mutable.{After, SpecWithJUnit}
import org.specs2.specification.Scope
import org.specs2.execute.PendingUntilFixed._

import scala.concurrent.duration._


class ASyncHttpClientTest extends HttpClientTest with ASyncHttpClientSupport

class SyncHttpClientTest extends HttpClientTest with SyncHttpClientSupport

abstract class HttpClientTest extends SpecWithJUnit { self: HttpClientSupport[_] =>


  trait ctx extends Scope with StubWebServerProvider


  "HttpClient" should {

    "support generating async get, post, put, patch, delete, options and trace request" in new ctx {
      Seq(get -> "Get", post -> "Post", put -> "Put",
        patch -> "Patch", delete -> "Delete",
        options -> "Options"/*, head -> "Head"*/, trace -> "Trace")
        .foreach { case (method, methodName) =>
          val path = s"$methodName/$randomStr"
          server.clearRecordedRequests()

          method(path)

          server must receivedRequestWith(methodName, toPath = path)
        }
    }

    "throw timeout if response takes more than default timeout" in {
      val server = aStubWebServer.addHandler( { case _ => Thread.sleep(100); HttpResponse() } )
                                 .build
                                 .start()

      get("/somePath", withTimeout = 5.millis)(server.baseUri) must throwA[AskTimeoutException]

      server.stop()
      ok
    }.pendingUntilFixed("find how to define read timeout")


    "allow to customize request using request transformers" in new ctx {
      get("/somePath", but = withHeaders("h1" -> "v1") )

      server must receivedRequestWith("h1" -> "v1")
    }

    "add accept header" in new ctx {
      get("/somePath", but = withHeaders("Accept" -> XmlContent.toString) )

      server must receivedRequestWith("Accept" -> XmlContent.toString)
    }
  }
}


trait StubWebServerProvider extends After {
  val server = aStubWebServer.build
                             .start()

  def after = server.stop()

  lazy implicit val baseUri: BaseUri = server.baseUri
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
