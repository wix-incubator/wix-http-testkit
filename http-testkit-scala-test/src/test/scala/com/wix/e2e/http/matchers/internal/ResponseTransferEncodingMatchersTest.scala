package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.TransferEncodings
import akka.http.scaladsl.model.TransferEncodings._
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.{HttpResponseTestSupport, MatchersTestSupport}
import org.scalatest.Matchers._
import org.scalatest.WordSpec


class ResponseTransferEncodingMatchersTest extends WordSpec with MatchersTestSupport {

  trait ctx extends HttpResponseTestSupport


  "ResponseTransferEncodingMatchersTest" should {

    "support matching against chunked transfer encoding" in new ctx {
      aChunkedResponse should beChunkedResponse
      aResponseWithoutTransferEncoding should not( beChunkedResponse )
      aResponseWithTransferEncodings(compress) should not( beChunkedResponse )
      aResponseWithTransferEncodings(chunked) should beChunkedResponse
    }

    "failure message in case no transfer encoding header should state that response did not have the proper header" in new ctx {
      failureMessageFor(beChunkedResponse, matchedOn = aResponseWithoutTransferEncoding) shouldBe
        "Expected Chunked response while response did not contain `Transfer-Encoding` header"
    }

    "failure message in case transfer encoding header exists should state that transfer encoding has a different value" in new ctx {
      failureMessageFor(beChunkedResponse, matchedOn = aResponseWithTransferEncodings(compress, TransferEncodings.deflate)) shouldBe
        "Expected Chunked response while response has `Transfer-Encoding` header with values ['compress', 'deflate']"
    }

    "support matching against transfer encoding header values" in new ctx {
      aResponseWithTransferEncodings(compress) should haveTransferEncodings("compress")
      aResponseWithTransferEncodings(compress) should not( haveTransferEncodings("deflate") )
    }

    "support matching against transfer encoding header with multiple values, matcher will validate that response has all of the expected values" in new ctx {
      aResponseWithTransferEncodings(compress, deflate) should haveTransferEncodings("deflate", "compress")
      aResponseWithTransferEncodings(compress, deflate) should haveTransferEncodings("compress")
    }

    "properly match chunked encoding" in new ctx {
      aChunkedResponse should haveTransferEncodings("chunked")
      aChunkedResponseWith(compress) should haveTransferEncodings("compress", "chunked")
      aChunkedResponseWith(compress) should haveTransferEncodings("chunked")
    }

    "failure message should describe what was the expected transfer encodings and what was found" in new ctx {
      failureMessageFor(haveTransferEncodings("deflate", "compress"), matchedOn = aChunkedResponseWith(gzip)) shouldBe
        s"Expected transfer encodings ['deflate', 'compress'] does not match actual transfer encoding ['chunked', 'gzip']"
    }

    "failure message in case no Transfer-Encoding for response should be handled" in new ctx {
      failureMessageFor(haveTransferEncodings("chunked"), matchedOn = aResponseWithoutTransferEncoding) shouldBe
        "Response did not contain `Transfer-Encoding` header."
    }

    "failure message if someone tries to match content-type in headers matchers" in new ctx {
      failureMessageFor(haveAllHeadersOf(transferEncodingHeader), matchedOn = aResponseWithContentType(contentType)) shouldBe
        """`Transfer-Encoding` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `beChunkedResponse` or `haveTransferEncodings` matcher instead.""".stripMargin
      failureMessageFor(haveAnyHeadersOf(transferEncodingHeader), matchedOn = aResponseWithContentType(contentType)) shouldBe
        """`Transfer-Encoding` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `beChunkedResponse` or `haveTransferEncodings` matcher instead.""".stripMargin
      failureMessageFor(haveTheSameHeadersAs(transferEncodingHeader), matchedOn = aResponseWithContentType(contentType)) shouldBe
        """`Transfer-Encoding` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `beChunkedResponse` or `haveTransferEncodings` matcher instead.""".stripMargin
    }
  }
}

