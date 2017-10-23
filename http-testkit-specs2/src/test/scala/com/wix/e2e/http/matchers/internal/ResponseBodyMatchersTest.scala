package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.MarshallingTestObjects.SomeCaseClass
import com.wix.e2e.http.matchers.drivers.{CustomMarshallerProvider, HttpResponseTestSupport, MarshallerTestSupport, MatchersTestSupport}
import org.specs2.matcher.ResultMatchers._
import org.specs2.mutable.Spec
import org.specs2.specification.Scope

class ResponseBodyMatchersTest extends Spec with MatchersTestSupport {

  trait ctxNoMarshaller extends Scope with HttpResponseTestSupport with MarshallerTestSupport
  trait ctx extends ctxNoMarshaller with CustomMarshallerProvider


  "ResponseBodyMatchers" should {

    "exact match on response body" in new ctx {
      aResponseWith(content) must haveBodyWith(content)
      aResponseWith(content) must not( haveBodyWith(anotherContent) )
    }

    "match underlying matcher with body content" in new ctx {
      aResponseWith(content) must haveBodyThat(must = be_===( content ))
      aResponseWith(content) must not( haveBodyThat(must = be_===( anotherContent )) )
    }

    "exact match on response binary body" in new ctx {
      aResponseWith(binaryContent) must haveBodyWith(binaryContent)
      aResponseWith(binaryContent) must not( haveBodyWith(anotherBinaryContent) )
    }

    "match underlying matcher with binary body content" in new ctx {
      aResponseWith(binaryContent) must haveBodyDataThat(must = be_===( binaryContent ))
      aResponseWith(binaryContent) must not( haveBodyDataThat(must = be_===( anotherBinaryContent )) )
    }

    "handle empty body" in new ctx {
      aResponseWithoutBody must not( haveBodyWith(content))
    }

    "support unmarshalling body content with user custom unmarshaller" in new ctx {
      givenUnmarshallerWith[SomeCaseClass](someObject, forContent = content)

      aResponseWith(content) must haveBodyWith(entity = someObject)
      aResponseWith(content) must not( haveBodyWith(entity = anotherObject) )
    }

    "provide a meaningful explanation why match failed" in new ctx {
      givenUnmarshallerWith[SomeCaseClass](someObject, forContent = content)

      failureMessageFor(haveBodyThat(must = be_===(anotherObject)), matchedOn = aResponseWith(content)) must_===
        s"Failed to match: ['$someObject' is not equal to '$anotherObject'] with content: [$content]"
    }

    "provide a proper message to user in case of a badly behaving marshaller" in new ctx {
      givenBadlyBehavingUnmarshallerFor[SomeCaseClass](withContent = content)

      haveBodyWith(entity = someObject).apply( aResponseWith(content) ) must beError(s"Failed to unmarshall: \\[$content\\]")
    }

    "support custom matcher for user object" in new ctx {
      givenUnmarshallerWith[SomeCaseClass](someObject, forContent = content)

      aResponseWith(content) must haveBodyThat(must = be_===(someObject))
      aResponseWith(content) must not( haveBodyThat(must = be_===(anotherObject)) )
    }

    "provide a default json marshaller in case no marshaller is specified" in new ctxNoMarshaller {
      aResponseWith(Marshaller.marshaller.marshall(someObject)) must haveBodyWith(someObject)
    }
  }
}
