package com.wix.e2e.http.server

import com.wix.e2e.http.BaseUri
import com.wix.e2e.http.client.sync._
import com.wix.e2e.http.drivers.HttpClientTestSupport
import com.wix.e2e.http.drivers.HttpServerMatchers._
import com.wix.e2e.http.exceptions.MisconfiguredMockServerException
import com.wix.e2e.http.matchers.RequestMatchers.{beGet, havePath}
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.server.WebServerFactory._
import org.specs2.mutable.Spec
import org.specs2.specification.Scope


class WebServerContractTest extends Spec {

  trait ctx extends Scope with HttpClientTestSupport


  "Embedded Web Server lifecycle" should {
    // todo: re-enable this
//    "be not available until started" in new ctx {
//      val server = aStubWebServer.onPort(somePort)
//                                 .build
//
//      get("/")(server.baseUri) must beConnectionRefused
//    }

    "throw an exception is server did not explicitly define a port and is queried for port or baseUri" in new ctx {
      val server = aStubWebServer.build

      server.baseUri must throwA[IllegalStateException]
    }

    "return port and base uri if server was created with explicit port" in new ctx {
      val server = aStubWebServer.onPort(somePort)
                                 .build

      server.baseUri must_=== BaseUri(port = somePort)
    }

    "allocate port for server once it's started" in new ctx {
      val server = aStubWebServer.build
                                 .start()

      server.baseUri

      server.stop()
    }

    "once server is stopped it will not be available" in new ctx {
      val server = aStubWebServer.build
                                 .start()
      val sut = server.baseUri
      server.stop()

      get("/")(sut) must beConnectionRefused
      server.baseUri must throwA[IllegalStateException]
    }
  }


  "Stub web server" should {
    "return 200Ok on all non defined handlers" in new ctx {
      val server = aStubWebServer.build
                                 .start()

      implicit lazy val sut = server.baseUri

      Seq(get, post, put, delete, patch, options, head, trace)
          .foreach { method =>
            method(path) must beSuccessful
          }
    }


    "record all incoming requests" in new ctx {
      val server = aStubWebServer.build
                                 .start()
      get(path)(server.baseUri)

      server.recordedRequests must contain( beGet and havePath(s"/$path") )
    }

    "reset recorded requests" in new ctx {
      val server = aStubWebServer.build
                                 .start()
      get(path)(server.baseUri)

      server.clearRecordedRequests()

      server.recordedRequests must beEmpty
    }

    "allow to define custom handlers" in new ctx {
      val server = aStubWebServer.addHandler(handlerFor(path, returnsBody = content))
                                 .build
                                 .start()

      get(path)(server.baseUri) must beSuccessfulWith(content)
    }

    "allow to dynamically append handlers" in new ctx {
      val server = aStubWebServer.build
                                 .start()

      server.appendAll(handlerFor(path, returnsBody = content))

      get(path)(server.baseUri) must beSuccessfulWith(content)
    }

    "allow to dynamically replace handlers" in new ctx {
      val server = aStubWebServer.addHandler(handlerFor(path, returnsBody = content))
                                 .build
                                 .start()

      server.replaceWith(handlerFor(path, returnsBody = anotherContent))

      get(path)(server.baseUri) must beSuccessfulWith(anotherContent)
    }

    "have an http server header including version" in new ctx {
      val server = aStubWebServer.build
                                 .start()

      get(path)(server.baseUri) must haveServerHttpTestkitHeaderWithLibraryVersion
    }
  }

  "Mock web server" should {

    "define at least one handler and respond according to the defined behavior" in new ctx {
      val server = aMockWebServerWith(handlerFor(path, returnsBody = content)).build
                                                                              .start()

      get(path)(server.baseUri) must beSuccessfulWith(content)
    }

    "allow to dynamically replace handlers" in new ctx {
      val server = aMockWebServerWith(handlerFor(path, returnsBody = content)).build
                                                                              .start()

      server.replaceWith(handlerFor(path, returnsBody = anotherContent))

      get(path)(server.baseUri) must beSuccessfulWith(anotherContent)
    }

    "return 404 if no handler is found to handle the request" in new ctx {
      val server = aMockWebServerWith(handlerFor(path, returnsBody = content)).build
                                                                              .start()

      implicit lazy val sut = server.baseUri

      get(anotherPath) must beNotFound
    }

    "allow server to be created with a seq of handlers" in new ctx {
      val server = aMockWebServerWith(Seq(handlerFor(path, returnsBody = content))).build
                                                                                   .start()

      get(path)(server.baseUri) must beSuccessfulWith(content)
    }

    "not allow server to be created with no handlers" in new ctx {
      val server = aMockWebServerWith(Seq.empty).build must throwAn[MisconfiguredMockServerException]
    }

    "have an http server header including version" in new ctx {
      val server = aMockWebServerWith(Seq(handlerFor(path, returnsBody = content))).build
                                                                                   .start()

      get(path)(server.baseUri) must haveServerHttpTestkitHeaderWithLibraryVersion
    }
  }
}
