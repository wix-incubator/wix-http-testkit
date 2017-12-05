package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods._
import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.HttpRequestFactory._
import org.scalatest.Matchers._
import org.scalatest.WordSpec

import scala.util.Random


class RequestMethodMatchersTest extends WordSpec {

  trait ctx {
    def randomMethodThatIsNot(method: HttpMethod) =
      Random.shuffle(Seq(CONNECT, DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT, TRACE).filterNot( _ == method)).head
  }

  "RequestMethodMatchers" should {

    "match all request methods" in new ctx {
      Seq(POST -> bePost, GET -> beGet, PUT -> bePut, DELETE -> beDelete,
          HEAD -> beHead, OPTIONS -> beOptions,
          PATCH -> bePatch, TRACE -> beTrace, CONNECT -> beConnect)
        .foreach { case (method, matcherForMethod) =>

          aRequestWith( method ) should matcherForMethod
          aRequestWith( randomMethodThatIsNot( method )) should not( matcherForMethod )
        }
    }
  }
}