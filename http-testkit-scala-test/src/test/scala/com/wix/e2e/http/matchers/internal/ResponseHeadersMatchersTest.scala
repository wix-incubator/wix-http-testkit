package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.{HttpResponseTestSupport, MatchersTestSupport}
import org.scalatest.Matchers._
import org.scalatest.WordSpec


class ResponseHeadersMatchersTest extends WordSpec with MatchersTestSupport {

  trait ctx extends HttpResponseTestSupport

  "ResponseHeadersMatchers" should {

    "contain header will check if any header is present" in new ctx {
      aResponseWithHeaders(header, anotherHeader) should haveAnyHeadersOf(header)
    }

    "return detailed message on hasAnyOf match failure" in new ctx {
      failureMessageFor(haveAnyHeadersOf(header, anotherHeader), matchedOn = aResponseWithHeaders(yetAnotherHeader, andAnotherHeader)) shouldBe
        s"Could not find header [${header._1}, ${anotherHeader._1}] but found those: [${yetAnotherHeader._1}, ${andAnotherHeader._1}]"
    }

    "contain header will check if all headers are present" in new ctx {
      aResponseWithHeaders(header, anotherHeader, yetAnotherHeader) should haveAllHeadersOf(header, anotherHeader)
    }

    "allOf matcher will return a message stating what was found, and what is missing from header list" in new ctx {
      failureMessageFor(haveAllHeadersOf(header, anotherHeader), matchedOn = aResponseWithHeaders(yetAnotherHeader, header)) shouldBe
        s"Could not find header [${anotherHeader._1}] but found those: [${header._1}]."
    }

    "same header as will check if the same headers is present" in new ctx {
      aResponseWithHeaders(header, anotherHeader) should haveTheSameHeadersAs(header, anotherHeader)
      aResponseWithHeaders(header, anotherHeader) should not( haveTheSameHeadersAs(header) )
      aResponseWithHeaders(header) should not( haveTheSameHeadersAs(header, anotherHeader) )
    }

    "haveTheSameHeadersAs matcher will return a message stating what was found, and what is missing from header list" in new ctx {
      failureMessageFor(haveTheSameHeadersAs(header, anotherHeader), matchedOn = aResponseWithHeaders(yetAnotherHeader, header)) shouldBe
        s"Request header is not identical, missing headers from request: [${anotherHeader._1}], request contained extra headers: [${yetAnotherHeader._1}]."
    }

    "header name compare should be case insensitive" in new ctx {
      aResponseWithHeaders(header) should haveAnyHeadersOf(header.copy(_1 = header._1.toUpperCase))
      aResponseWithHeaders(header) should not( haveAnyHeadersOf(header.copy(_2 = header._2.toUpperCase)) )

      aResponseWithHeaders(header) should haveAllHeadersOf(header.copy(_1 = header._1.toUpperCase))
      aResponseWithHeaders(header) should not( haveAllHeadersOf(header.copy(_2 = header._2.toUpperCase)) )

      aResponseWithHeaders(header) should haveTheSameHeadersAs(header.copy(_1 = header._1.toUpperCase))
      aResponseWithHeaders(header) should not( haveTheSameHeadersAs(header.copy(_2 = header._2.toUpperCase)) )
    }

    "request with no headers will show a 'no headers' message" in new ctx {
      failureMessageFor(haveAnyHeadersOf(header), matchedOn = aResponseWithNoHeaders ) shouldBe
        "Response did not contain any headers."

      failureMessageFor(haveAllHeadersOf(header), matchedOn = aResponseWithNoHeaders ) shouldBe
        "Response did not contain any headers."

      failureMessageFor(haveTheSameHeadersAs(header), matchedOn = aResponseWithNoHeaders ) shouldBe
        "Response did not contain any headers."
    }

    "ignore cookies and set cookies from headers comparison" in new ctx {
      aResponseWithCookies(cookie) should not( haveAnyHeadersOf("Set-Cookie" -> s"${cookie.name}=${cookie.value}") )
      aResponseWithCookies(cookie) should not( haveAllHeadersOf("Set-Cookie" -> s"${cookie.name}=${cookie.value}") )
      aResponseWithCookies(cookie) should not( haveTheSameHeadersAs("Set-Cookie" -> s"${cookie.name}=${cookie.value}") )
    }

    "match if any header satisfy the composed matcher" in new ctx {
      aResponseWithHeaders(header) should haveAnyHeaderThat(must = be(header._2), withHeaderName = header._1)
      aResponseWithHeaders(header) should not( haveAnyHeaderThat(must = be(anotherHeader._2), withHeaderName = header._1) )
    }

    "return informative error messages" in new ctx {
      failureMessageFor(haveAnyHeaderThat(must = AlwaysMatcher(), withHeaderName = nonExistingHeaderName), matchedOn = aResponseWithHeaders(header)) shouldBe
        s"Response contain header names: [${header._1}] which did not contain: [$nonExistingHeaderName]"
      failureMessageFor(haveAnyHeaderThat(must = AlwaysMatcher(), withHeaderName = nonExistingHeaderName), matchedOn = aResponseWithNoHeaders) shouldBe
        "Response did not contain any headers."
      failureMessageFor(haveAnyHeaderThat(must = be(anotherHeader._2), withHeaderName = header._1), matchedOn = aResponseWithHeaders(header)) shouldBe
        s"Response header [${header._1}], did not match { ${be(anotherHeader._2).apply(header._2).failureMessage} }"
    }
  }
}