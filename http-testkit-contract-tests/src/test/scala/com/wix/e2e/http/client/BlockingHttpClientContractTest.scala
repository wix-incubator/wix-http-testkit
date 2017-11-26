package com.wix.e2e.http.client

import akka.http.scaladsl.model.HttpResponse
import com.wix.e2e.http.api.Marshaller.Implicits._
import com.wix.e2e.http.client.sync._
import com.wix.e2e.http.drivers.{HttpClientTestSupport, StubWebServerProvider}
import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.ResponseMatchers
import com.wix.e2e.http.matchers.ResponseMatchers.beConnectionRefused
import com.wix.e2e.http.server.WebServerFactory.aStubWebServer
import org.specs2.mutable.Spec
import org.specs2.specification.Scope

import scala.concurrent.TimeoutException
import scala.concurrent.duration._

class BlockingHttpClientContractTest extends Spec {

  trait ctx extends Scope with StubWebServerProvider with HttpClientTestSupport


  "BlockingHttpClient" should {

    "support url with parameters" in new ctx {
      get(s"$path?${parameter._1}=${parameter._2}",
        apply = withHeader(header)
          and withCookie(cookie) )

      server must receivedAnyRequestThat( beGet and
                                          havePath(s"/$path") and
                                          haveTheSameParamsAs(parameter) and
                                          haveAnyHeadersOf(header) and
                                          receivedCookieWith(cookie._1))
    }

    "support generating get request" in new ctx {
      get(path,
          apply = withParam(parameter)
            and withHeader(header)
            and withCookie(cookie) )

      server must receivedAnyRequestThat( beGet and
                                          havePath(s"/$path") and
                                          haveTheSameParamsAs(parameter) and
                                          haveAnyHeadersOf(header) and
                                          receivedCookieWith(cookie._1))
    }

    "support modification of user agent" in new ctx {
      get(path, apply = withUserAgent(userAgent) )

      server must receivedAnyRequestThat( haveAnyHeadersOf("user-agent" -> userAgent))
    }

    "support generating post request" in new ctx {
      post(path,
           apply = withParam(parameter)
             and withHeader(header)
             and withCookie(cookie)
             and withPayload(someObject))

      server must receivedAnyRequestThat( bePost and
                                          havePath(s"/$path") and
                                          haveTheSameParamsAs(parameter) and
                                          haveAnyHeadersOf(header) and
                                          receivedCookieWith(cookie._1) and
                                          haveBodyWith(someObject))
    }

    "support generating put request" in new ctx {
      put(path,
          apply = withParam(parameter)
            and withHeader(header)
            and withCookie(cookie)
            and withPayload(someObject))

      server must receivedAnyRequestThat( bePut and
                                          havePath(s"/$path") and
                                          haveTheSameParamsAs(parameter) and
                                          haveAnyHeadersOf(header) and
                                          receivedCookieWith(cookie._1) and
                                          haveBodyWith(someObject))
    }

    "support generating delete request" in new ctx {
      delete(path,
             apply = withParam(parameter)
               and withHeader(header)
               and withCookie(cookie)
               and withPayload(someObject))

      server must receivedAnyRequestThat( beDelete and
                                          havePath(s"/$path") and
                                          haveTheSameParamsAs(parameter) and
                                          haveAnyHeadersOf(header) and
                                          receivedCookieWith(cookie._1) and
                                          haveBodyWith(someObject))
    }

    "support generating patch request" in new ctx {
      patch(path,
            apply = withParam(parameter)
              and withHeader(header)
              and withCookie(cookie)
              and withPayload(someObject))

      server must receivedAnyRequestThat( bePatch and
                                          havePath(s"/$path") and
                                          haveTheSameParamsAs(parameter) and
                                          haveAnyHeadersOf(header) and
                                          receivedCookieWith(cookie._1) and
                                          haveBodyWith(someObject))
    }

    "support generating options request" in new ctx {
      options(path,
            apply = withParam(parameter)
              and withHeader(header)
              and withCookie(cookie)
              and withPayload(someObject))

      server must receivedAnyRequestThat( beOptions and
                                          havePath(s"/$path") and
                                          haveTheSameParamsAs(parameter) and
                                          haveAnyHeadersOf(header) and
                                          receivedCookieWith(cookie._1) and
                                          haveBodyWith(someObject))
    }

    "support generating trace request" in new ctx {
      trace(path,
            apply = withParam(parameter)
              and withHeader(header)
              and withCookie(cookie))

      server must receivedAnyRequestThat( beTrace and
                                          havePath(s"/$path") and
                                          haveTheSameParamsAs(parameter) and
                                          haveAnyHeadersOf(header) and
                                          receivedCookieWith(cookie._1))
    }

    "support generating head request" in new ctx {
      head(path,
            apply = withParam(parameter)
              and withHeader(header)
              and withCookie(cookie))

      server must receivedAnyRequestThat( beHead and
                                          havePath(s"/$path") and
                                          haveTheSameParamsAs(parameter) and
                                          haveAnyHeadersOf(header) and
                                          receivedCookieWith(cookie._1))
    }

    "throw timeout if response takes more than default timeout" in {
      val server = aStubWebServer.addHandler( { case _ => Thread.sleep(500); HttpResponse() } )
                                 .build
                                 .start()

      get("/somePath", withTimeout = 5.millis)(server.baseUri) must throwA[TimeoutException]
    }

    "match connection failed" in new ctx {
      get(path)(ClosedPort) must beConnectionRefused
    }

    "returns response with pre-consumed entity (frees up connection)" in new ctx {
      server.appendAll(bigResponseWith(size = bigResponse))

      get("/big-response") must
        ResponseMatchers.beSuccessfulWithBodyThat(must = haveSize(bigResponse))
    }
  }
}
