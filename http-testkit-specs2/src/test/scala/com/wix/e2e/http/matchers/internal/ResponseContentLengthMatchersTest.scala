package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.{HttpMessageTestSupport, MatchersTestSupport}
import org.specs2.mutable.Spec
import org.specs2.specification.Scope


class ResponseContentLengthMatchersTest extends Spec with MatchersTestSupport {

  trait ctx extends Scope with HttpMessageTestSupport

  "ResponseContentLengthMatchers" should {

    "support matching against specific content length" in new ctx {
      aResponseWith(contentWith(length = length)) must haveContentLength(length = length)
      aResponseWith(contentWith(length = anotherLength)) must not( haveContentLength(length = length) )
    }

    "support matching content length against response without content length" in new ctx {
      aResponseWithoutContentLength must not( haveContentLength(length = length) )
    }

    "support matching against response without content length" in new ctx {
      aResponseWithoutContentLength must haveNoContentLength
      aResponseWith(contentWith(length = length)) must not( haveNoContentLength )
    }

    "failure message should describe what was the expected content length and what was found" in new ctx {
      failureMessageFor(haveContentLength(length = length), matchedOn = aResponseWith(contentWith(length = anotherLength))) must_===
        s"Expected content length [$length] does not match actual content length [$anotherLength]"
    }

    "failure message should reflect that content length header was not found" in new ctx {
      failureMessageFor(haveContentLength(length = length), matchedOn = aResponseWithoutContentLength) must_===
        s"Expected content length [$length] but response did not contain `content-length` header."
    }

    "failure message should reflect that content length header exists while trying to match against a content length that doesn't exists" in new ctx {
      failureMessageFor(haveNoContentLength, matchedOn = aResponseWith(contentWith(length = length))) must_===
        s"Expected no `content-length` header but response did contain `content-length` header with size [$length]."
    }

    "failure message if someone tries to match content-length in headers matchers" in new ctx {
      failureMessageFor(haveAllHeadersOf(contentLengthHeader), matchedOn = aResponseWithContentType(contentType)) must_===
        """`Content-Length` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `haveContentLength` matcher instead.""".stripMargin
      failureMessageFor(haveAnyHeadersOf(contentLengthHeader), matchedOn = aResponseWithContentType(contentType)) must_===
        """`Content-Length` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `haveContentLength` matcher instead.""".stripMargin
      failureMessageFor(haveTheSameHeadersAs(contentLengthHeader), matchedOn = aResponseWithContentType(contentType)) must_===
        """`Content-Length` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `haveContentLength` matcher instead.""".stripMargin
    }
  }
}

