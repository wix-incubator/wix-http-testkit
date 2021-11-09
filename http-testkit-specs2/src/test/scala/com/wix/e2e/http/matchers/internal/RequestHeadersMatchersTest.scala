package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.HttpRequestFactory._
import com.wix.e2e.http.matchers.drivers.{HttpMessageTestSupport, MatchersTestSupport}
import com.wix.test.random.randomStr
import org.specs2.matcher.AlwaysMatcher
import org.specs2.mutable.Spec
import org.specs2.specification.Scope


class RequestHeadersMatchersTest extends Spec with MatchersTestSupport {

  trait ctx extends Scope with HttpMessageTestSupport

  "RequestHeadersMatchers" should {

    "contain header will check if any header is present" in new ctx {
      aRequestWithHeaders(header, anotherHeader) must haveAnyHeadersOf(header)
    }

    "return detailed message on hasAnyOf match failure" in new ctx {
      failureMessageFor(haveAnyHeadersOf(header, anotherHeader), matchedOn = aRequestWithHeaders(yetAnotherHeader, andAnotherHeader)) must_===
        s"Could not find header [${header._1}, ${anotherHeader._1}] but found those: [${yetAnotherHeader._1}, ${andAnotherHeader._1}]"
    }

    "contain header will check if all headers are present" in new ctx {
      aRequestWithHeaders(header, anotherHeader, yetAnotherHeader) must haveAllHeadersOf(header, anotherHeader)
    }

    "allOf matcher will return a message stating what was found, and what is missing from header list" in new ctx {
      failureMessageFor(haveAllHeadersOf(header, anotherHeader), matchedOn = aRequestWithHeaders(yetAnotherHeader, header)) must_===
        s"Could not find header [${anotherHeader._1}] but found those: [${header._1}]."
    }

    "allOf matcher returns a message describing the different contents if the name is correct but the data is different" in new ctx {
      failureMessageFor(haveAllHeadersOf(header1Name -> header1Content, header2Name -> header2Content),
        matchedOn = aRequestWithHeaders(header1Name -> another1HeaderContent, header2Name -> another2HeaderContent)) must_===
        s"Found headers with different content: [$header1Name -> ($header1Content != $another1HeaderContent), $header2Name -> ($header2Content != $another2HeaderContent)]."
    }

    "haveAllHeadersOf matcher returns a message describing the different contents and different headers" in new ctx {
      failureMessageFor(haveAllHeadersOf(header1Name -> header1Content, header),
        matchedOn = aRequestWithHeaders(header1Name -> another1HeaderContent, anotherHeader)) must_===
        s"Could not find header [${header._1}] but found those: []. Also found headers with different content: [$header1Name -> ($header1Content != $another1HeaderContent)]."
    }

    "same header as will check if the same headers is present" in new ctx {
      aRequestWithHeaders(header, anotherHeader) must haveTheSameHeadersAs(header, anotherHeader)
      aRequestWithHeaders(header, anotherHeader) must not( haveTheSameHeadersAs(header) )
      aRequestWithHeaders(header) must not( haveTheSameHeadersAs(header, anotherHeader) )
    }

    "haveTheSameHeadersAs matcher will return a message stating what was found, and what is missing from header list" in new ctx {
      failureMessageFor(haveTheSameHeadersAs(header, anotherHeader), matchedOn = aRequestWithHeaders(yetAnotherHeader, header)) must_===
        s"Request header is not identical, missing headers from request: [${anotherHeader._1}], request contained extra headers: [${yetAnotherHeader._1}]."
    }

    "haveTheSameHeaderAs matcher returns a message describing the different contents if the name is correct but the data is different" in new ctx {
      failureMessageFor(haveTheSameHeadersAs(header1Name -> header1Content, header),
        matchedOn = aRequestWithHeaders(header1Name -> another1HeaderContent, anotherHeader)) must_===
        s"Request header is not identical, missing headers from request: [${header._1}], request contained extra headers: [${anotherHeader._1}]. Also found headers with different content: [$header1Name -> ($header1Content != $another1HeaderContent)]."
    }

    "header name compare should be case insensitive" in new ctx {
      aRequestWithHeaders(header) must haveAnyHeadersOf(header.copy(_1 = header._1.toUpperCase))
      aRequestWithHeaders(header) must not(haveAnyHeadersOf(header.copy(_2 = header._2.toUpperCase)))
    }

    "header name compare should be case insensitive1" in new ctx {
      aRequestWithHeaders(header) must haveAllHeadersOf(header.copy(_1 = header._1.toUpperCase))
      aRequestWithHeaders(header) must not(haveAllHeadersOf(header.copy(_2 = header._2.toUpperCase)))
    }

    "header name compare should be case insensitive2" in new ctx {
      aRequestWithHeaders(header) must haveTheSameHeadersAs(header.copy(_1 = header._1.toUpperCase))
      aRequestWithHeaders(header) must not( haveTheSameHeadersAs(header.copy(_2 = header._2.toUpperCase)) )
    }

    "request with no headers will show a 'no headers' message" in new ctx {
      failureMessageFor(haveAnyHeadersOf(header), matchedOn = aRequestWithNoHeaders ) must_===
        "Request did not contain any headers."

      failureMessageFor(haveAllHeadersOf(header), matchedOn = aRequestWithNoHeaders ) must_===
        "Request did not contain any headers."

      failureMessageFor(haveTheSameHeadersAs(header), matchedOn = aRequestWithNoHeaders ) must_===
        "Request did not contain any headers."
    }

    "ignore cookies and set cookies from headers comparison" in new ctx {
      aRequestWithCookies(cookiePair) must not( haveAnyHeadersOf("Cookie" -> s"${cookiePair._1}=${cookiePair._2}") )
      aRequestWithCookies(cookiePair) must not( haveAllHeadersOf("Cookie" -> s"${cookiePair._1}=${cookiePair._2}") )
      aRequestWithCookies(cookiePair) must not( haveTheSameHeadersAs("Cookie" -> s"${cookiePair._1}=${cookiePair._2}") )
      aRequestWithCookies(cookiePair) must not( haveAnyHeaderThat(must = be_===(s"${cookiePair._1}=${cookiePair._2}"), withHeaderName = "Cookie") )
    }

    "match if any header satisfy the composed matcher" in new ctx {
      aRequestWithHeaders(header) must haveAnyHeaderThat(must = be_===(header._2), withHeaderName = header._1)
      aRequestWithHeaders(header) must not( haveAnyHeaderThat(must = be_===(anotherHeader._2), withHeaderName = header._1) )
    }

    "return informative error messages" in new ctx {
      failureMessageFor(haveAnyHeaderThat(must = AlwaysMatcher(), withHeaderName = nonExistingHeaderName), matchedOn = aRequestWithHeaders(header)) must_===
        s"Request contain header names: [${header._1}] which did not contain: [$nonExistingHeaderName]"
      failureMessageFor(haveAnyHeaderThat(must = AlwaysMatcher(), withHeaderName = nonExistingHeaderName), matchedOn = aRequestWithNoHeaders) must_===
        "Request did not contain any headers."
      failureMessageFor(haveAnyHeaderThat(must = be_===(anotherHeader._2), withHeaderName = header._1), matchedOn = aRequestWithHeaders(header)) must_===
        s"Request header [${header._1}], did not match { ${be_===(anotherHeader._2).apply(header._2).message} }"
    }
  }
}