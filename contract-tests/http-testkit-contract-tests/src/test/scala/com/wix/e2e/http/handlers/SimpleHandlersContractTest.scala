package com.wix.e2e.http.handlers

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import com.wix.e2e.http.RequestHandler
import com.wix.e2e.http.client.sync._
import com.wix.e2e.http.handlers.Handlers.stringHandler
import com.wix.e2e.http.matchers.Matchers._
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers._
import com.wix.e2e.http.server.WebServerFactory.aMockWebServer
import org.specs2.mutable.Spec
import org.specs2.specification.Scope

class SimpleHandlersContractTest extends Spec {

  trait ctx extends Scope {
    val server = aMockWebServer.build.start()

    val okHandler: RequestHandler = { case _ => HttpResponse(status = StatusCodes.OK) }
  }

  "Simple handler" should {
    "allow to return string response on any request" in new ctx {
      val validStringResponse = "Hello world"

      server.appendAll(stringHandler(validStringResponse))

      get("/")(server.baseUri) must beSuccessfulWith(validStringResponse)
    }

    "allow to apply path matcher on handlers" in new ctx {
      server.appendAll(okHandler matchWith havePath("/hello/world"))

      get("/hello/world")(server.baseUri) must beSuccessful
      get("/hello/world/")(server.baseUri) must beSuccessful
      get("hello/world/")(server.baseUri) must beSuccessful
      get("/hello")(server.baseUri) must beNotFound
    }

    "support wildcard in path matcher" in new ctx {
      server.appendAll(okHandler matchWith havePath("*/world/*"))

      get("/hello/world/!")(server.baseUri) must beSuccessful
      get("/bye-bye/world/!")(server.baseUri) must beSuccessful
      get("/world")(server.baseUri) must beNotFound
    }

    "allow to apply query param matcher on handlers" in new ctx {
      server.appendAll(haveQueryParams("a" -> "b", "c" -> "d") handleWith okHandler)

      get("/", but = withParams("a" -> "b", "c" -> "d", "x" -> "y"))(server.baseUri) must beSuccessful
      get("/", but = withParam("c" -> "d"))(server.baseUri) must beNotFound
    }

    "allow to apply both path matcher and query param matcher on handlers" in new ctx {
      server.appendAll(haveQueryParams("a" -> "b", "c" -> "d") && havePath("ololo") handleWith okHandler)

      get("/ololo", but = withParams("a" -> "b", "c" -> "d", "x" -> "y"))(server.baseUri) must beSuccessful
      get("/", but = withParams("a" -> "b", "c" -> "d", "x" -> "y"))(server.baseUri) must beNotFound
      get("/ololo", but = withParam("c" -> "d"))(server.baseUri) must beNotFound
    }

  }
}
