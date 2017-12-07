package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.RequestRecorderFactory._
import com.wix.e2e.http.matchers.drivers.{MatchersTestSupport, RequestRecorderTestSupport}
import org.specs2.matcher.AlwaysMatcher
import org.specs2.mutable.Spec
import org.specs2.specification.Scope

class RequestRecorderMatchersTest extends Spec with MatchersTestSupport {

  trait ctx extends Scope with RequestRecorderTestSupport

  "RequestRecorderMatchers" should {

    "check that request recorder has any of the given requests" in new ctx {
      aRequestRecorderWith(request, anotherRequest) must receivedAnyOf(request)
      aRequestRecorderWith(request) must not( receivedAnyOf(anotherRequest) )
    }

    "return detailed message on hasAnyOf match failure" in new ctx {
      failureMessageFor(receivedAnyOf(request, anotherRequest), matchedOn = aRequestRecorderWith(yetAnotherRequest, yetAnotherRequest)) must_===
        s"""Could not find requests:
           |1: $request,
           |2: $anotherRequest
           |
           |but found those:
           |1: $yetAnotherRequest,
           |2: $yetAnotherRequest""".stripMargin
    }

    "contain header will check if all requests are present" in new ctx {
      aRequestRecorderWith(request, anotherRequest, yetAnotherRequest) must receivedAllOf(request, anotherRequest)
      aRequestRecorderWith(request) must not( receivedAllOf(request, anotherRequest) )
    }

    "allOf matcher will return a message stating what was found, and what is missing from recorded requests list" in new ctx {
      failureMessageFor(receivedAllOf(request, anotherRequest), matchedOn = aRequestRecorderWith(request, yetAnotherRequest)) must_===
        s"""Could not find requests:
            |1: $anotherRequest
            |
            |but found those:
            |1: $request""".stripMargin
    }

    "same request as will check if the same requests is present" in new ctx {
      aRequestRecorderWith(request, anotherRequest) must receivedTheSameRequestsAs(request, anotherRequest)
      aRequestRecorderWith(request, anotherRequest) must not( receivedTheSameRequestsAs(request) )
      aRequestRecorderWith(request) must not( receivedTheSameRequestsAs(request, anotherRequest) )
    }

    "receivedTheSameRequestsAs matcher will return a message stating what was found, and what is missing from header list" in new ctx {
      failureMessageFor(receivedTheSameRequestsAs(request, anotherRequest), matchedOn = aRequestRecorderWith(request, yetAnotherRequest)) must_===
        s"""Requests are not identical, missing requests are:
            |1: $anotherRequest
            |
            |added requests found:
            |1: $yetAnotherRequest""".stripMargin
    }

    "if no recorded requests were found, error message returned will be 'no requests' message" in new ctx {
      failureMessageFor(receivedAnyOf(request), matchedOn = anEmptyRequestRecorder ) must_===
        "Server did not receive any requests."

      failureMessageFor(receivedAllOf(request), matchedOn = anEmptyRequestRecorder ) must_===
        "Server did not receive any requests."

      failureMessageFor(receivedTheSameRequestsAs(request), matchedOn = anEmptyRequestRecorder ) must_===
        "Server did not receive any requests."
    }

    "match if any request satisfy the composed matcher" in new ctx {
      aRequestRecorderWith(request) must receivedAnyRequestThat(must = be_===(request))
      aRequestRecorderWith(request) must not( receivedAnyRequestThat(must = be_===(anotherRequest)) )
    }

    "return informative error messages" in new ctx {
      failureMessageFor(receivedAnyRequestThat(must = be_===(anotherRequest)), matchedOn = aRequestRecorderWith(request)) must_===
        s"""Could not find any request that matches:
           |1: ${ be_===(anotherRequest).apply(request).message.replaceAll("\n", "") }""".stripMargin
      failureMessageFor(receivedAnyRequestThat(must = AlwaysMatcher()), matchedOn = anEmptyRequestRecorder) must_===
        "Server did not receive any requests."
    }
  }
}
