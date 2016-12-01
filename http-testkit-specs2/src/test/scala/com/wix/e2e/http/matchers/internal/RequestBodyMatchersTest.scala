package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.json.DefaultMarshaller
import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.HttpRequestFactory._
import com.wix.e2e.http.matchers.drivers.MarshallingTestObjects.SomeCaseClass
import com.wix.e2e.http.matchers.drivers.{CustomMarshallerProvider, HttpResponseTestSupport, MarshallerTestSupport, MatchersTestSupport}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class RequestBodyMatchersTest extends SpecWithJUnit with MatchersTestSupport {

  trait ctxNoMarshaller extends Scope with HttpResponseTestSupport with MarshallerTestSupport
  trait ctx extends ctxNoMarshaller with CustomMarshallerProvider


  "ResponseBodyMatchers" should {

    "exact match on response body" in new ctx {
      aRequestWith(content) must haveBodyWith(content)
      aRequestWith(content) must not( haveBodyWith(anotherContent) )
    }

    "match underlying matcher with body content" in new ctx {
      aRequestWith(content) must haveBodyThat(must = be_===( content ))
      aRequestWith(content) must not( haveBodyThat(must = be_===( anotherContent )) )
    }

    "exact match on response binary body" in new ctx {
      aRequestWith(binaryContent) must haveBodyWith(binaryContent)
      aRequestWith(binaryContent) must not( haveBodyWith(anotherBinaryContent) )
    }

    "match underlying matcher with binary body content" in new ctx {
      aRequestWith(binaryContent) must haveBodyDataThat(must = be_===( binaryContent ))
      aRequestWith(binaryContent) must not( haveBodyDataThat(must = be_===( anotherBinaryContent )) )
    }

    "handle empty body" in new ctx {
      aRequestWithoutBody must not( haveBodyWith(content))
    }

    "support unmarshalling body content with user custom unmarshaller" in new ctx {
      givenUnmarshallerWith[SomeCaseClass](someObject, forContent = content)

      aRequestWith(content) must havePayloadWith(entity = someObject)
      aRequestWith(content) must not( havePayloadWith(entity = anotherObject) )
    }

    "provide a meaningful explanation why match failed" in new ctx {
      givenUnmarshallerWith[SomeCaseClass](someObject, forContent = content)

      failureMessageFor(havePayloadThat(must = be_===(anotherObject)), matchedOn = aRequestWith(content)) must_===
        s"Failed to match: ['$someObject' is not equal to '$anotherObject'] with content: [$content]"
    }

    "provide a proper message to user in case of a badly behaving marshaller" in new ctx {
      givenBadlyBehavingUnmarshallerFor[SomeCaseClass](withContent = content)

      failureMessageFor(havePayloadWith(entity = someObject), matchedOn = aRequestWith(content)) must_===
        s"Failed to unmarshall: [$content]"
    }

    "support custom matcher for user object" in new ctx {
      givenUnmarshallerWith[SomeCaseClass](someObject, forContent = content)

      aRequestWith(content) must havePayloadThat(must = be_===(someObject))
      aRequestWith(content) must not( havePayloadThat(must = be_===(anotherObject)) )
    }

    "provide a default json marshaller in case no marshaller is specified" in new ctxNoMarshaller {
      aRequestWith(DefaultMarshaller.marshaller.marshall(someObject)) must havePayloadWith(someObject)
    }
  }
}

