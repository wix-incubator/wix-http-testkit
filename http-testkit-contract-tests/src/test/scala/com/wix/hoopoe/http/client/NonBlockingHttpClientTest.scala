package com.wix.hoopoe.http.client

import akka.http.scaladsl.model.HttpResponse
import com.wix.hoopoe.http.drivers.StubWebServerMatchers._
import com.wix.hoopoe.http.drivers.StubWebServerProvider
import com.wix.hoopoe.http.matchers.ResponseMatchers._
import com.wix.hoopoe.http.server.WebServerFactory.aStubWebServer
import com.wix.hoopoe.http.utils._
import com.wixpress.hoopoe.test._
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

import scala.concurrent.TimeoutException
import scala.concurrent.duration._

class NonBlockingHttpClientTest extends SpecWithJUnit with NonBlockingHttpClientSupport with FutureMatchers {

  trait ctx extends Scope with StubWebServerProvider

  implicit val ee = ExecutionEnv.fromGlobalExecutionContext

  "NonBlockingHttpClient" should {

    "support generating get, post, put, patch, delete, options and trace request" in new ctx {
      Seq(get -> "Get", post -> "Post", put -> "Put",
          patch -> "Patch", delete -> "Delete",
          options -> "Options"/*, head -> "Head"*/, trace -> "Trace")
        .foreach { case (method, methodName) =>
          val path = s"$methodName/$randomStr"
          server.clearRecordedRequests()

          method(path) must beSuccessful.await

          server must receivedRequestWith(methodName, toPath = path)
        }
    }

    "throw timeout if response takes more than default timeout" in {
      val server = aStubWebServer.addHandler( { case _ => Thread.sleep(500); HttpResponse() } )
                                 .build
                                 .start()

      waitFor( get("/somePath", withTimeout = 5.millis)(server.baseUri) ) must throwA[TimeoutException]
    }

    "allow to customize request using request transformers" in new ctx {
      get("/somePath", but = withHeaders("h1" -> "v1") )

      server must receivedRequestWith("h1" -> "v1")
    }

    "add accept header" in new ctx {
      get("/somePath", but = withHeaders("Accept" -> XmlContent.toString) )

      server must receivedRequestWith("Accept" -> XmlContent.toString)
    }

    "match connection failed" in new ctx {
      get("/nowhere")(ClosedPort) must beConnectionRefused.await
    }
  }
}
