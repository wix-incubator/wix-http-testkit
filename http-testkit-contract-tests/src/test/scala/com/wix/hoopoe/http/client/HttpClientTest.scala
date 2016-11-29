package com.wix.hoopoe.http.client

import akka.http.scaladsl.model.HttpResponse
import akka.pattern.AskTimeoutException
import com.wix.hoopoe.http.drivers.StubWebServerMatchers._
import com.wix.hoopoe.http.drivers.StubWebServerProvider
import com.wix.hoopoe.http.server.WebServerFactory.aStubWebServer
import com.wixpress.hoopoe.test._
import org.specs2.mutable.SpecWithJUnit
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


