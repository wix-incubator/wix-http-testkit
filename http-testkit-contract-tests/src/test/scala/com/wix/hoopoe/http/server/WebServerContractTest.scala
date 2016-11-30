package com.wix.hoopoe.http.server

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.Uri.Path
import com.wix.hoopoe.http._
import com.wix.hoopoe.http.client.sync._
import com.wix.hoopoe.http.matchers.ResponseMatchers._
import com.wix.hoopoe.http.server.RequestMatchers._
import com.wix.hoopoe.http.server.WebServerFactory._
import com.wixpress.hoopoe.test._
import org.specs2.matcher.Matcher
import org.specs2.matcher.Matchers._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class WebServerContractTest extends SpecWithJUnit {

  trait ctx extends Scope {
    private def randomPort = randomInt(0, 65535)

    val somePort = randomPort
    val somePath = randomStr
    val anotherPath = randomStr
    val content = randomStr

    def handlerFor(path: String, returnsBody: String): RequestHandler = {
      case r: HttpRequest if r.uri.path.toString.endsWith(path) => HttpResponse(entity = returnsBody)
    }
  }

  "Embedded Web Server lifecycle" should {
    "be not available until started" in new ctx {
      val server = aStubWebServer.onPort(somePort)
                                 .build

      get("/")(server.baseUri) must beConnectionRefused
    }

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
            method(somePath) must beSuccessful
          }
    }


    "record all incoming requests" in new ctx {
      val server = aStubWebServer.build
                                 .start()
      get(somePath)(server.baseUri)

      server.recordedRequests must contain( beGetRequestWith(path = somePath) )
    }

    "reset recorded requests" in new ctx {
      val server = aStubWebServer.build
                                 .start()
      get(somePath)(server.baseUri)

      server.clearRecordedRequests()

      server.recordedRequests must beEmpty
    }

    "allow to define custom handlers" in new ctx {
      val server = aStubWebServer.addHandler(handlerFor(somePath, returnsBody = content))
                                 .build
                                 .start()

      get(somePath)(server.baseUri) must beSuccessfulWith(content)
    }
  }

  "Mock web server" should {

    "define at least one handler and respond according to the defined behavior" in new ctx {
      val server = aMockWebServerWith(handlerFor(somePath, returnsBody = content)).build
                                                                                  .start()

      implicit lazy val sut = server.baseUri

      get(somePath) must beSuccessfulWith(content)
    }

    "return 404 if no handler is found to handle the request" in new ctx {
      val server = aMockWebServerWith(handlerFor(somePath, returnsBody = content)).build
                                                                                  .start()

      implicit lazy val sut = server.baseUri

      get(anotherPath) must beNotFound
    }
  }
}

object RequestMatchers {
  def beGetRequestWith(path: String): Matcher[HttpRequest] =
    be_===( s"/$path" ) ^^ { (_: HttpRequest).uri.path.toString() aka "request path" }
}
