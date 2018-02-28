package com.wix.e2e.http.handlers

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
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

  trait ctx extends Scope {
    val server = aMockWebServer.build.start()
    implicit val baseUri = server.baseUri
    implicit val marshaller: Marshaller = new JsonJacksonMarshaller

    val ok: HttpResponse = HttpResponse(status = StatusCodes.OK)
  }


  "Simple handler" should {
    "allow to return string response on any request" in new ctx {
      private val validStringResponse = "Hello world"

      server.appendAll(HttpResponse(entity = validStringResponse))

      get("/") must beSuccessfulWith(validStringResponse)
    }

    "allow to apply path matcher on handlers" in new ctx {
      server.appendAll(havePath("/hello/world") respond ok)

      get("/hello/world") must beSuccessful
      get("/hello/world/") must beSuccessful
      get("hello/world/") must beSuccessful
      get("/hello") must beNotFound
    }

    "support wildcard in path matcher" in new ctx {
      server.appendAll(havePath("*/world/*") respond ok)

      get("/hello/world/!") must beSuccessful
      get("/bye-bye/world/!") must beSuccessful
      get("/world") must beNotFound
    }

    "allow to apply query param matcher on handlers" in new ctx {
      server.appendAll(haveQueryParams("a" -> "b", "c" -> "d") respond ok)

      get("/", but = withParams("a" -> "b", "c" -> "d", "x" -> "y")) must beSuccessful
      get("/", but = withParam("c" -> "d")) must beNotFound
    }

    "allow to apply both path matcher and query param matcher on handlers" in new ctx {
      server.appendAll(haveQueryParams("a" -> "b", "c" -> "d") and havePath("ololo") respond ok)

      get("/ololo", but = withParams("a" -> "b", "c" -> "d", "x" -> "y")) must beSuccessful
      get("/", but = withParams("a" -> "b", "c" -> "d", "x" -> "y")) must beNotFound
      get("/ololo", but = withParam("c" -> "d")) must beNotFound
    }

    "allow to respond with case class" in new ctx {
      val response = SimpleEntityResponse("privet!")

      server.appendAll(forAnyRequest respondEntity response)

      get("/arbitrary/path") must beSuccessfulWith(response)
    }

  }
}

case class SimpleEntityResponse(response: String)
