package com.wix.hoopoe.http.client

import akka.http.scaladsl.model.HttpResponse
import com.wix.hoopoe.http.drivers.{HttpClientTestSupport, StubWebServerProvider}
import com.wix.hoopoe.http.matchers.RequestMatchers._
import com.wix.hoopoe.http.matchers.ResponseMatchers.beConnectionRefused
import com.wix.hoopoe.http.server.WebServerFactory.aStubWebServer
import com.wix.hoopoe.http.utils._
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

import scala.concurrent.TimeoutException
import scala.concurrent.duration._

class NonBlockingHttpClientTest extends SpecWithJUnit with NonBlockingHttpClientSupport with FutureMatchers {

  trait ctx extends Scope with StubWebServerProvider with HttpClientTestSupport


  implicit val ee = ExecutionEnv.fromGlobalExecutionContext

  "NonBlockingHttpClient" should {

    "support generating get request" in new ctx {
      get(path,
        but = withParam(parameter)
          and withHeader(header)
          and withCookie(cookie) )

      server.recordedRequests must contain( beGet and
                                            havePath(s"/$path") and
                                            haveAnyOf(header) and
                                            receivedCookieWith(cookie._1)).eventually
    }

    "support generating post request" in new ctx {
      post(path,
        but = withParam(parameter)
          and withHeader(header)
          and withCookie(cookie)
          and withPayload(someObject))

      server.recordedRequests must contain( bePost and
                                            havePath(s"/$path") and
                                            haveAnyOf(header) and
                                            receivedCookieWith(cookie._1) and
                                            havePayloadWith(someObject)).eventually
    }

    "support generating put request" in new ctx {
      put(path,
        but = withParam(parameter)
          and withHeader(header)
          and withCookie(cookie)
          and withPayload(someObject))

      server.recordedRequests must contain( bePut and
                                            havePath(s"/$path") and
                                            haveAnyOf(header) and
                                            receivedCookieWith(cookie._1) and
                                            havePayloadWith(someObject)).eventually
    }

    "support generating delete request" in new ctx {
      delete(path,
        but = withParam(parameter)
          and withHeader(header)
          and withCookie(cookie)
          and withPayload(someObject))

      server.recordedRequests must contain( beDelete and
                                            havePath(s"/$path") and
                                            haveAnyOf(header) and
                                            receivedCookieWith(cookie._1) and
                                            havePayloadWith(someObject)).eventually
    }

    "support generating patch request" in new ctx {
      patch(path,
        but = withParam(parameter)
          and withHeader(header)
          and withCookie(cookie)
          and withPayload(someObject))

      server.recordedRequests must contain( bePatch and
                                            havePath(s"/$path") and
                                            haveAnyOf(header) and
                                            receivedCookieWith(cookie._1) and
                                            havePayloadWith(someObject)).eventually
    }

    "support generating options request" in new ctx {
      options(path,
        but = withParam(parameter)
          and withHeader(header)
          and withCookie(cookie)
          and withPayload(someObject))

      server.recordedRequests must contain( beOptions and
                                            havePath(s"/$path") and
                                            haveAnyOf(header) and
                                            receivedCookieWith(cookie._1) and
                                            havePayloadWith(someObject)).eventually
    }

    "support generating trace request" in new ctx {
      trace(path,
        but = withParam(parameter)
          and withHeader(header)
          and withCookie(cookie))

      server.recordedRequests must contain( beTrace and
                                            havePath(s"/$path") and
                                            haveAnyOf(header) and
                                            receivedCookieWith(cookie._1)).eventually
    }

    "throw timeout if response takes more than default timeout" in {
      val server = aStubWebServer.addHandler( { case _ => Thread.sleep(500); HttpResponse() } )
                                 .build
                                 .start()

      waitFor( get("/somePath", withTimeout = 5.millis)(server.baseUri) ) must throwA[TimeoutException]
    }

    "match connection failed" in new ctx {
      get("/nowhere")(ClosedPort) must beConnectionRefused.await
    }
  }
}
