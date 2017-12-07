package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.{HttpMessageTestSupport, MatchersTestSupport}
import org.scalatest.Matchers._
import org.scalatest.WordSpec


class ResponseContentLengthMatchersTest extends WordSpec with MatchersTestSupport {

  trait ctx extends HttpMessageTestSupport

  "ResponseContentLengthMatchers" should {

    "support matching against specific content length" in new ctx {
      aResponseWith(contentWith(length = length)) should haveContentLength(length = length)
      aResponseWith(contentWith(length = anotherLength)) should not( haveContentLength(length = length) )
    }

    "support matching content length against response without content length" in new ctx {
      aResponseWithoutContentLength should not( haveContentLength(length = length) )
    }

    "support matching against response without content length" in new ctx {
      aResponseWithoutContentLength should haveNoContentLength
      aResponseWith(contentWith(length = length)) should not( haveNoContentLength )
    }

    "failure message should describe what was the expected content length and what was found" in new ctx {
      failureMessageFor(haveContentLength(length = length), matchedOn = aResponseWith(contentWith(length = anotherLength))) shouldBe
        s"Expected content length [$length] does not match actual content length [$anotherLength]"
    }

    "failure message should reflect that content length header was not found" in new ctx {
      failureMessageFor(haveContentLength(length = length), matchedOn = aResponseWithoutContentLength) shouldBe
        s"Expected content length [$length] but response did not contain `content-length` header."
    }

    "failure message should reflect that content length header exists while trying to match against a content length that doesn't exists" in new ctx {
      failureMessageFor(haveNoContentLength, matchedOn = aResponseWith(contentWith(length = length))) shouldBe
        s"Expected no `content-length` header but response did contain `content-length` header with size [$length]."
    }

    "failure message if someone tries to match content-length in headers matchers" in new ctx {
      failureMessageFor(haveAllHeadersOf(contentLengthHeader), matchedOn = aResponseWithContentType(contentType)) shouldBe
        """`Content-Length` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `haveContentLength` matcher instead.""".stripMargin
      failureMessageFor(haveAnyHeadersOf(contentLengthHeader), matchedOn = aResponseWithContentType(contentType)) shouldBe
        """`Content-Length` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `haveContentLength` matcher instead.""".stripMargin
      failureMessageFor(haveTheSameHeadersAs(contentLengthHeader), matchedOn = aResponseWithContentType(contentType)) shouldBe
        """`Content-Length` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `haveContentLength` matcher instead.""".stripMargin
    }
  }
}

