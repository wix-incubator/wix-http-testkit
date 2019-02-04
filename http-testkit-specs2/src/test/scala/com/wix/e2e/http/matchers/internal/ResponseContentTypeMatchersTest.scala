package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.{HttpMessageTestSupport, MatchersTestSupport}
import org.specs2.mutable.Spec
import org.specs2.specification.Scope


class ResponseContentTypeMatchersTest extends Spec with MatchersTestSupport {

  trait ctx extends Scope with HttpMessageTestSupport


  "ResponseContentTypeMatchers" should {

    "support matching against json content type" in new ctx {
      aResponseWithContentType("application/json") must beJsonResponse
      aResponseWithContentType("text/plain") must not( beJsonResponse )
    }

    "support matching against text plain content type" in new ctx {
      aResponseWithContentType("text/plain") must beTextPlainResponse
      aResponseWithContentType("application/json") must not( beTextPlainResponse )
    }

    "support matching against form url encoded content type" in new ctx {
      aResponseWithContentType("application/x-www-form-urlencoded") must beFormUrlEncodedResponse
      aResponseWithContentType("application/json") must not( beFormUrlEncodedResponse )
    }

    "show proper error in case matching against a malformed content type" in new ctx {
      failureMessageFor(haveContentType(malformedContentType), matchedOn = aResponseWithContentType(anotherContentType)) must
        contain(s"Cannot match against a malformed content type: $malformedContentType")
    }

    "support matching against content type" in new ctx {
      aResponseWithContentType(contentType) must haveContentType(contentType)
    }

    "failure message should describe what was the expected content type and what was found" in new ctx {
      failureMessageFor(haveContentType(contentType), matchedOn = aResponseWithContentType(anotherContentType)) must_===
        s"Expected content type [$contentType] does not match actual content type [$anotherContentType]"
    }

    "failure message in case no content type for body should be handled" in new ctx {
      failureMessageFor(haveContentType(contentType), matchedOn = aResponseWithoutBody) must_===
        "Response body does not have a set content type"
    }

    "failure message if someone tries to match content-type in headers matchers" in new ctx {
      failureMessageFor(haveAllHeadersOf(contentTypeHeader), matchedOn = aResponseWithContentType(contentType)) must_===
        """`Content-Type` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `haveContentType` matcher instead (or `beJsonResponse`, `beTextPlainResponse`, `beFormUrlEncodedResponse`).""".stripMargin
      failureMessageFor(haveAnyHeadersOf(contentTypeHeader), matchedOn = aResponseWithContentType(contentType)) must_===
        """`Content-Type` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `haveContentType` matcher instead (or `beJsonResponse`, `beTextPlainResponse`, `beFormUrlEncodedResponse`).""".stripMargin
      failureMessageFor(haveTheSameHeadersAs(contentTypeHeader), matchedOn = aResponseWithContentType(contentType)) must_===
        """`Content-Type` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `haveContentType` matcher instead (or `beJsonResponse`, `beTextPlainResponse`, `beFormUrlEncodedResponse`).""".stripMargin
    }
  }
}
