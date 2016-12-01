package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.json.Marshaller
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.HttpResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseTestSupport
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class ResponseBodyAndStatusMatchersTest extends SpecWithJUnit {

  trait ctx extends Scope with HttpResponseTestSupport


  "ResponseBodyAndStatusMatchers" should {

    "match successful request with body content" in new ctx {
      aSuccessfulResponseWith(content) must beSuccessfulWith(content)
      aSuccessfulResponseWith(content) must not( beSuccessfulWith(anotherContent) )
    }

    "match successful request with body content matcher" in new ctx {
      aSuccessfulResponseWith(content) must beSuccessfulWithBodyThat(must = be_===( content ))
      aSuccessfulResponseWith(content) must not( beSuccessfulWithBodyThat(must = be_===( anotherContent )) )
    }

    "match invalid request with body content" in new ctx {
      anInvalidResponseWith(content) must beInvalidWith(content)
      anInvalidResponseWith(content) must not( beInvalidWith(anotherContent) )
    }

    "match invalid request with body content matcher" in new ctx {
      anInvalidResponseWith(content) must beInvalidWithBodyThat(must = be_===( content ))
      anInvalidResponseWith(content) must not( beInvalidWithBodyThat(must = be_===( anotherContent )) )
    }

    "match successful request with binary body content" in new ctx {
      aSuccessfulResponseWith(binaryContent) must beSuccessfulWith(binaryContent)
      aSuccessfulResponseWith(binaryContent) must not( beSuccessfulWith(anotherBinaryContent) )
    }

    "match successful request with binary body content matcher" in new ctx {
      aSuccessfulResponseWith(binaryContent) must beSuccessfulWithBodyDataThat(must = be_===( binaryContent ))
      aSuccessfulResponseWith(binaryContent) must not( beSuccessfulWithBodyDataThat(must = be_===( anotherBinaryContent )) )
    }

    "match successful request with entity" in new ctx {
      aSuccessfulResponseWith(Marshaller.marshaller.marshall(someObject)) must beSuccessfulWith( someObject )
      aSuccessfulResponseWith(Marshaller.marshaller.marshall(someObject)) must not( beSuccessfulWith( anotherObject ) )
    }

    "match successful request with entity with custom marshaller" in new ctx {
      implicit val marshaller = Marshaller.marshaller
      aSuccessfulResponseWith(Marshaller.marshaller.marshall(someObject)) must beSuccessfulWith( someObject )
      aSuccessfulResponseWith(Marshaller.marshaller.marshall(someObject)) must not( beSuccessfulWith( anotherObject ) )
    }

    "match successful request with entity matcher" in new ctx {
      aSuccessfulResponseWith(Marshaller.marshaller.marshall(someObject)) must beSuccessfulWithEntityThat( must = be_===( someObject ) )
      aSuccessfulResponseWith(Marshaller.marshaller.marshall(someObject)) must not( beSuccessfulWithEntityThat( must = be_===( anotherObject ) ) )
    }

    "match successful request with headers" in new ctx {
      aSuccessfulResponseWith(header, anotherHeader) must beSuccessfulWithHeaders(header, anotherHeader)
      aSuccessfulResponseWith(header) must not( beSuccessfulWithHeaders(anotherHeader) )
    }

    "match successful request with header matcher" in new ctx {
      aSuccessfulResponseWith(header) must beSuccessfulWithHeaderThat(must = be_===(header._2), withHeaderName = header._1)
      aSuccessfulResponseWith(header) must not( beSuccessfulWithHeaderThat(must = be_===(anotherHeader._2), withHeaderName = header._1) )
    }

    "match successful request with cookies" in new ctx {
      aSuccessfulResponseWithCookies(cookie, anotherCookie) must beSuccessfulWithCookie(cookie.name)
      aSuccessfulResponseWithCookies(cookie) must not( beSuccessfulWithCookie(anotherCookie.name) )
    }

    "match successful request with cookie matcher" in new ctx {
      aSuccessfulResponseWithCookies(cookie) must beSuccessfulWithCookieThat(must = cookieWith(cookie.value))
      aSuccessfulResponseWithCookies(cookie) must not( beSuccessfulWithCookieThat(must = cookieWith(anotherCookie.value)) )
    }
  }
}