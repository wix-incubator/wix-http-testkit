package com.wix.e2e.http.handlers

import akka.http.scaladsl.model.HttpResponse
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.client.sync._
import com.wix.e2e.http.json.JsonJacksonMarshaller
import com.wix.e2e.http.matchers.Matchers._
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers._
import com.wix.e2e.http.server.WebServerFactory.aMockWebServer
import org.specs2.mutable.Spec
import org.specs2.specification.Scope

class SimpleHandlersContractTest extends Spec {

  trait ctx extends Scope with Responses {
    val server = aMockWebServer.build.start()
    implicit val baseUri = server.baseUri
    implicit val marshaller: Marshaller = new JsonJacksonMarshaller

    val privetResponse = SimpleEntityResponse("privet!")
    val pakaResponse = SimpleEntityResponse("paka!")

  }

  "Simple handler" should {
    "allow to return string response on any request" in new ctx {
      private val validStringResponse = "Hello world"

      server.appendAll(HttpResponse(entity = validStringResponse))

      get("/") must beSuccessfulWith(validStringResponse)
    }

    "allow to apply path matcher on handlers" in new ctx {
      server.appendAll(havePath("/hello/world") respond ok())

      get("/hello/world") must beSuccessful
      get("/hello/world/") must beSuccessful
      get("hello/world/") must beSuccessful
      get("/hello") must beNotFound
    }

    "support wildcard in path matcher" in new ctx {
      server.appendAll(havePath("*/world/*") respond ok())

      get("/hello/world/!") must beSuccessful
      get("/bye-bye/world/!") must beSuccessful
      get("/world") must beNotFound
    }

    "allow to apply query param matcher on handlers" in new ctx {
      server.appendAll(haveQueryParams("a" -> "b", "c" -> "d") respond ok())

      get("/", but = withParams("a" -> "b", "c" -> "d", "x" -> "y")) must beSuccessful
      get("/", but = withParam("c" -> "d")) must beNotFound
    }

    "allow to apply both path matcher and query param matcher on handlers" in new ctx {
      server.appendAll(haveQueryParams("a" -> "b", "c" -> "d") and havePath("ololo") respond ok())

      get("/ololo", but = withParams("a" -> "b", "c" -> "d", "x" -> "y")) must beSuccessful
      get("/", but = withParams("a" -> "b", "c" -> "d", "x" -> "y")) must beNotFound
      get("/ololo", but = withParam("c" -> "d")) must beNotFound
    }

    "allow to respond with case class" in new ctx {
      val response = SimpleEntityResponse("privet!")

      server.appendAll(forAnyRequest respond ok(response))

      get("/arbitrary/path") must beSuccessfulWith(response)
    }

    "allow to match via body" in new ctx {
      server.appendAll(haveBody(beTypedEqualTo(privetResponse)) respond ok())

      post("/arbitrary/path", but = withPayload(privetResponse)) must beSuccessful
      post("/arbitrary/path", but = withPayload(pakaResponse)) must beNotFound
    }

    "allow to apply all together" in new ctx {
      server.appendAll(havePath("/users/*") and haveQueryParams("a" -> "x") and haveBody(beTypedEqualTo(privetResponse)) respond ok())

      post("/users/1", but = withPayload(privetResponse) and withParam("a" -> "x")) must beSuccessful
      post("/users/1", but = withPayload(privetResponse)) must beNotFound
      post("/users/1", but = withParam("a" -> "x")) must beNotFound
      get("/users/1", but = withParam("a" -> "x")) must beNotFound
      post("/users", but = withPayload(privetResponse) and withParam("a" -> "x")) must beNotFound
      post("/users/1", but = withPayload(pakaResponse) and withParam("a" -> "x")) must beNotFound
    }
  }
}

case class SimpleEntityResponse(response: String)
