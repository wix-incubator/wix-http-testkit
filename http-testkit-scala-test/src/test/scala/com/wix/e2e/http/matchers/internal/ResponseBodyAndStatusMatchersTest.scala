package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.api.Marshaller.Implicits.marshaller
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.HttpResponseMatchers._
import com.wix.e2e.http.matchers.drivers.MarshallingTestObjects.SomeCaseClass
import com.wix.e2e.http.matchers.drivers.{HttpMessageTestSupport, MatchersTestSupport}
import org.scalatest.Matchers._
import org.scalatest._


class ResponseBodyAndStatusMatchersTest extends WordSpec with MatchersTestSupport {

  trait ctx extends HttpMessageTestSupport

  "ResponseBodyAndStatusMatchers" should {

    "match successful request with body content" in new ctx {
      aSuccessfulResponseWith(content) should beSuccessfulWith(content)
      aSuccessfulResponseWith(content) should not( beSuccessfulWith(anotherContent) )
    }

    "provide a proper message to user sent a matcher to an entity matcher" in new ctx {
      failureMessageFor(beSuccessfulWith(entity = be(content)), matchedOn = aResponseWith(content)) shouldBe
        s"Matcher misuse: `beSuccessfulWith` received a matcher to match against, please use `beSuccessfulWithEntityThat` instead."
    }

    "match successful request with body content matcher" in new ctx {
      aSuccessfulResponseWith(content) should beSuccessfulWithBodyThat(must = be( content ))
      aSuccessfulResponseWith(content) should not( beSuccessfulWithBodyThat(must = be( anotherContent )) )
    }

    "match invalid request with body content" in new ctx {
      anInvalidResponseWith(content) should beInvalidWith(content)
      anInvalidResponseWith(content) should not( beInvalidWith(anotherContent) )
    }

    "match invalid request with body content matcher" in new ctx {
      anInvalidResponseWith(content) should beInvalidWithBodyThat(must = be( content ))
      anInvalidResponseWith(content) should not( beInvalidWithBodyThat(must = be( anotherContent )) )
    }

    "match successful request with binary body content" in new ctx {
      aSuccessfulResponseWith(binaryContent) should beSuccessfulWith(binaryContent)
      aSuccessfulResponseWith(binaryContent) should not( beSuccessfulWith(anotherBinaryContent) )
    }

    "match successful request with binary body content matcher" in new ctx {
      aSuccessfulResponseWith(binaryContent) should beSuccessfulWithBodyDataThat(must = be( binaryContent ))
      aSuccessfulResponseWith(binaryContent) should not( beSuccessfulWithBodyDataThat(must = be( anotherBinaryContent )) )
    }

    "match successful request with entity" in new ctx {
      aSuccessfulResponseWith(marshaller.marshall(someObject)) should beSuccessfulWith( someObject )
      aSuccessfulResponseWith(marshaller.marshall(someObject)) should not( beSuccessfulWith( anotherObject ) )
    }

    "match successful request with entity with custom marshaller" in new ctx {
      aSuccessfulResponseWith(marshaller.marshall(someObject)) should beSuccessfulWith( someObject )
      aSuccessfulResponseWith(marshaller.marshall(someObject)) should not( beSuccessfulWith( anotherObject ) )
    }

    "match successful request with entity matcher" in new ctx {
      aSuccessfulResponseWith(marshaller.marshall(someObject)) should beSuccessfulWithEntityThat[SomeCaseClass]( must = be( someObject ) )
      aSuccessfulResponseWith(marshaller.marshall(someObject)) should not( beSuccessfulWithEntityThat[SomeCaseClass]( must = be( anotherObject ) ) )
    }

    "match successful request with headers" in new ctx {
      aSuccessfulResponseWith(header, anotherHeader) should beSuccessfulWithHeaders(header, anotherHeader)
      aSuccessfulResponseWith(header) should not( beSuccessfulWithHeaders(anotherHeader) )
    }

    "match successful request with header matcher" in new ctx {
      aSuccessfulResponseWith(header) should beSuccessfulWithHeaderThat(must = be(header._2), withHeaderName = header._1)
      aSuccessfulResponseWith(header) should not( beSuccessfulWithHeaderThat(must = be(anotherHeader._2), withHeaderName = header._1) )
    }

    "match successful request with cookies" in new ctx {
      aSuccessfulResponseWithCookies(cookie, anotherCookie) should beSuccessfulWithCookie(cookie.name)
      aSuccessfulResponseWithCookies(cookie) should not( beSuccessfulWithCookie(anotherCookie.name) )
    }

    "match successful request with cookiePair matcher" in new ctx {
      aSuccessfulResponseWithCookies(cookie) should beSuccessfulWithCookieThat(must = cookieWith(cookie.value))
      aSuccessfulResponseWithCookies(cookie) should not( beSuccessfulWithCookieThat(must = cookieWith(anotherCookie.value)) )
    }

    "provide a proper message to user sent a matcher to an `haveBodyWith` matcher" in new ctx {
      failureMessageFor(haveBodyWith(entity = be(someObject)), matchedOn = aResponseWith(content)) shouldBe
        s"Matcher misuse: `haveBodyWith` received a matcher to match against, please use `haveBodyThat` instead."
    }
  }
}
