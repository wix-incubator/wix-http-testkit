package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.CommonTestMatchers._
import com.wix.e2e.http.matchers.drivers.RequestRecorderFactory._
import com.wix.e2e.http.matchers.drivers.{MatchersTestSupport, RequestRecordTestSupport}
import org.scalatest.Matchers._
import org.scalatest.WordSpec


class RequestRecorderMatchersTest extends WordSpec with MatchersTestSupport {

  trait ctx extends RequestRecordTestSupport

  "RequestRecorderMatchers" should {

    "check that request recorder has any of the given requests" in new ctx {
      aRequestRecorderWith(request, anotherRequest) should receivedAnyOf(request)
      aRequestRecorderWith(request) should not( receivedAnyOf(anotherRequest) )
    }

    "return detailed message on hasAnyOf match failure" in new ctx {
      failureMessageFor(receivedAnyOf(request, anotherRequest), matchedOn = aRequestRecorderWith(yetAnotherRequest, yetAnotherRequest)) shouldBe
        s"""Could not find requests:
           |1: $request,
           |2: $anotherRequest
           |
           |but found those:
           |1: $yetAnotherRequest,
           |2: $yetAnotherRequest""".stripMargin
    }

    "contain header will check if all requests are present" in new ctx {
      aRequestRecorderWith(request, anotherRequest, yetAnotherRequest) should receivedAllOf(request, anotherRequest)
      aRequestRecorderWith(request) should not( receivedAllOf(request, anotherRequest) )
    }

    "allOf matcher will return a message stating what was found, and what is missing from recorded requests list" in new ctx {
      failureMessageFor(receivedAllOf(request, anotherRequest), matchedOn = aRequestRecorderWith(request, yetAnotherRequest)) shouldBe
        s"""Could not find requests:
            |1: $anotherRequest
            |
            |but found those:
            |1: $request""".stripMargin
    }

    "same request as will check if the same requests is present" in new ctx {
      aRequestRecorderWith(request, anotherRequest) should receivedTheSameRequestsAs(request, anotherRequest)
      aRequestRecorderWith(request, anotherRequest) should not( receivedTheSameRequestsAs(request) )
      aRequestRecorderWith(request) should not( receivedTheSameRequestsAs(request, anotherRequest) )
    }

    "receivedTheSameRequestsAs matcher will return a message stating what was found, and what is missing from header list" in new ctx {
      failureMessageFor(receivedTheSameRequestsAs(request, anotherRequest), matchedOn = aRequestRecorderWith(request, yetAnotherRequest)) shouldBe
        s"""Requests are not identical, missing requests are:
            |1: $anotherRequest
            |
            |added requests found:
            |1: $yetAnotherRequest""".stripMargin
    }

    "if no recorded requests were found, error message returned will be 'no requests' message" in new ctx {
      failureMessageFor(receivedAnyOf(request), matchedOn = anEmptyRequestRecorder ) shouldBe
        "Server did not receive any requests."

      failureMessageFor(receivedAllOf(request), matchedOn = anEmptyRequestRecorder ) shouldBe
        "Server did not receive any requests."

      failureMessageFor(receivedTheSameRequestsAs(request), matchedOn = anEmptyRequestRecorder ) shouldBe
        "Server did not receive any requests."
    }

    "match if any request satisfy the composed matcher" in new ctx {
      aRequestRecorderWith(request) should receivedAnyRequestThat(must = be(request))
      aRequestRecorderWith(request) should not( receivedAnyRequestThat(must = be(anotherRequest)) )
    }

    "return informative error messages" in new ctx {
      failureMessageFor(receivedAnyRequestThat(must = be(anotherRequest)), matchedOn = aRequestRecorderWith(request)) shouldBe
        s"""Could not find any request that matches:
           |1: ${ be(anotherRequest).apply(request).failureMessage}""".stripMargin
      failureMessageFor(receivedAnyRequestThat(must = AlwaysMatcher()), matchedOn = anEmptyRequestRecorder) shouldBe
        "Server did not receive any requests."
    }
  }
}
