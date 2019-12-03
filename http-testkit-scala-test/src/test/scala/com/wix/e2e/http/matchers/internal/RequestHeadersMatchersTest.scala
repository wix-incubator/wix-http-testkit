package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.CommonTestMatchers._
import com.wix.e2e.http.matchers.drivers.HttpRequestFactory._
import com.wix.e2e.http.matchers.drivers.{HttpMessageTestSupport, MatchersTestSupport}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class RequestHeadersMatchersTest extends AnyWordSpec with MatchersTestSupport {

  trait ctx extends HttpMessageTestSupport

  "RequestHeadersMatchers" should {

    "contain header will check if any header is present" in new ctx {
      aRequestWithHeaders(header, anotherHeader) should haveAnyHeadersOf(header)
    }

    "return detailed message on hasAnyOf match failure" in new ctx {
      failureMessageFor(haveAnyHeadersOf(header, anotherHeader), matchedOn = aRequestWithHeaders(yetAnotherHeader, andAnotherHeader)) shouldBe
        s"Could not find header [${header._1}, ${anotherHeader._1}] but found those: [${yetAnotherHeader._1}, ${andAnotherHeader._1}]"
    }

    "contain header will check if all headers are present" in new ctx {
      aRequestWithHeaders(header, anotherHeader, yetAnotherHeader) should haveAllHeadersOf(header, anotherHeader)
    }

    "allOf matcher will return a message stating what was found, and what is missing from header list" in new ctx {
      failureMessageFor(haveAllHeadersOf(header, anotherHeader), matchedOn = aRequestWithHeaders(yetAnotherHeader, header)) shouldBe
        s"Could not find header [${anotherHeader._1}] but found those: [${header._1}]."
    }

    "same header as will check if the same headers is present" in new ctx {
      aRequestWithHeaders(header, anotherHeader) should haveTheSameHeadersAs(header, anotherHeader)
      aRequestWithHeaders(header, anotherHeader) should not( haveTheSameHeadersAs(header) )
      aRequestWithHeaders(header) should not( haveTheSameHeadersAs(header, anotherHeader) )
    }

    "haveTheSameHeadersAs matcher will return a message stating what was found, and what is missing from header list" in new ctx {
      failureMessageFor(haveTheSameHeadersAs(header, anotherHeader), matchedOn = aRequestWithHeaders(yetAnotherHeader, header)) shouldBe
        s"Request header is not identical, missing headers from request: [${anotherHeader._1}], request contained extra headers: [${yetAnotherHeader._1}]."
    }

    "header name compare should be case insensitive" in new ctx {
      aRequestWithHeaders(header) should haveAnyHeadersOf(header.copy(_1 = header._1.toUpperCase))
      aRequestWithHeaders(header) should not( haveAnyHeadersOf(header.copy(_2 = header._2.toUpperCase)) )

      aRequestWithHeaders(header) should haveAllHeadersOf(header.copy(_1 = header._1.toUpperCase))
      aRequestWithHeaders(header) should not( haveAllHeadersOf(header.copy(_2 = header._2.toUpperCase)) )

      aRequestWithHeaders(header) should haveTheSameHeadersAs(header.copy(_1 = header._1.toUpperCase))
      aRequestWithHeaders(header) should not( haveTheSameHeadersAs(header.copy(_2 = header._2.toUpperCase)) )
    }

    "request with no headers will show a 'no headers' message" in new ctx {
      failureMessageFor(haveAnyHeadersOf(header), matchedOn = aRequestWithNoHeaders ) shouldBe
        "Request did not contain any headers."

      failureMessageFor(haveAllHeadersOf(header), matchedOn = aRequestWithNoHeaders ) shouldBe
        "Request did not contain any headers."

      failureMessageFor(haveTheSameHeadersAs(header), matchedOn = aRequestWithNoHeaders ) shouldBe
        "Request did not contain any headers."
    }

    "ignore cookies and set cookies from headers comparison" in new ctx {
      aRequestWithCookies(cookiePair) should not( haveAnyHeadersOf("Cookie" -> s"${cookiePair._1}=${cookiePair._2}") )
      aRequestWithCookies(cookiePair) should not( haveAllHeadersOf("Cookie" -> s"${cookiePair._1}=${cookiePair._2}") )
      aRequestWithCookies(cookiePair) should not( haveTheSameHeadersAs("Cookie" -> s"${cookiePair._1}=${cookiePair._2}") )
      aRequestWithCookies(cookiePair) should not( haveAnyHeaderThat(must = be(s"${cookiePair._1}=${cookiePair._2}"), withHeaderName = "Cookie") )
    }

    "match if any header satisfy the composed matcher" in new ctx {
      aRequestWithHeaders(header) should haveAnyHeaderThat(must = be(header._2), withHeaderName = header._1)
      aRequestWithHeaders(header) should not( haveAnyHeaderThat(must = be(anotherHeader._2), withHeaderName = header._1) )
    }

    "return informative error messages" in new ctx {
      failureMessageFor(haveAnyHeaderThat(must = AlwaysMatcher(), withHeaderName = nonExistingHeaderName), matchedOn = aRequestWithHeaders(header)) shouldBe
        s"Request contain header names: [${header._1}] which did not contain: [$nonExistingHeaderName]"
      failureMessageFor(haveAnyHeaderThat(must = AlwaysMatcher(), withHeaderName = nonExistingHeaderName), matchedOn = aRequestWithNoHeaders) shouldBe
        "Request did not contain any headers."
      failureMessageFor(haveAnyHeaderThat(must = be(anotherHeader._2), withHeaderName = header._1), matchedOn = aRequestWithHeaders(header)) shouldBe
        s"Request header [${header._1}], did not match { ${be(anotherHeader._2).apply(header._2).failureMessage} }"
    }
  }
}
