package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.TransferEncodings
import akka.http.scaladsl.model.TransferEncodings._
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.{HttpResponseTestSupport, MatchersTestSupport}
import org.specs2.mutable.Spec
import org.specs2.specification.Scope


class ResponseTransferEncodingMatchersTest extends Spec with MatchersTestSupport {

  trait ctx extends Scope with HttpResponseTestSupport


  "ResponseTransferEncodingMatchersTest" should {

    "support matching against chunked transfer encoding" in new ctx {
      aChunkedResponse must beChunkedResponse
      aResponseWithoutTransferEncoding must not( beChunkedResponse )
      aResponseWithTransferEncodings(compress) must not( beChunkedResponse )
    }

    "failure message in case no transfer encoding header should state that response did not have the proper header" in new ctx {
      failureMessageFor(beChunkedResponse, matchedOn = aResponseWithoutTransferEncoding) must_===
        "Expected Chunked response while response did not contain `Transfer-Encoding` header"
    }

    "failure message in case transfer encoding header exists should state that transfer encoding has a different value" in new ctx {
      failureMessageFor(beChunkedResponse, matchedOn = aResponseWithTransferEncodings(compress, TransferEncodings.deflate)) must_===
        "Expected Chunked response while response has `Transfer-Encoding` header with values ['compress', 'deflate']"
    }

    "support matching against transfer encoding header values" in new ctx {
      aResponseWithTransferEncodings(compress) must haveTransferEncodings("compress")
      aResponseWithTransferEncodings(compress) must not( haveTransferEncodings("deflate") )
    }

    "support matching against transfer encoding header with multiple values, matcher will validate that response has all of the expected values" in new ctx {
      aResponseWithTransferEncodings(compress, deflate) must haveTransferEncodings("deflate", "compress")
      aResponseWithTransferEncodings(compress, deflate) must haveTransferEncodings("compress")
    }

    "properly match chunked encoding" in new ctx {
      aChunkedResponse must haveTransferEncodings("chunked")
      aChunkedResponseWith(compress) must haveTransferEncodings("compress", "chunked")
      aChunkedResponseWith(compress) must haveTransferEncodings("chunked")
    }

    "failure message should describe what was the expected transfer encodings and what was found" in new ctx {
      failureMessageFor(haveTransferEncodings("deflate", "compress"), matchedOn = aChunkedResponseWith(gzip)) must_===
        s"Expected transfer encodings ['deflate', 'compress'] does not match actual transfer encoding ['chunked', 'gzip']"
    }

    "failure message in case no Transfer-Encoding for response should be handled" in new ctx {
      failureMessageFor(haveTransferEncodings("chunked"), matchedOn = aResponseWithoutTransferEncoding) must_===
        "Response did not contain `Transfer-Encoding` header."
    }

    "failure message if someone tries to match content-type in headers matchers" in new ctx {
      failureMessageFor(haveAllHeadersOf(transferEncodingHeader), matchedOn = aResponseWithContentType(contentType)) must_===
        """`Transfer-Encoding` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `beChunkedResponse` or `haveTransferEncodings` matcher instead.""".stripMargin
      failureMessageFor(haveAnyHeadersOf(transferEncodingHeader), matchedOn = aResponseWithContentType(contentType)) must_===
        """`Transfer-Encoding` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `beChunkedResponse` or `haveTransferEncodings` matcher instead.""".stripMargin
      failureMessageFor(haveTheSameHeadersAs(transferEncodingHeader), matchedOn = aResponseWithContentType(contentType)) must_===
        """`Transfer-Encoding` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
          |Use `beChunkedResponse` or `haveTransferEncodings` matcher instead.""".stripMargin
    }
  }
}

