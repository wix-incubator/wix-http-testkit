package com.wix.hoopoe.http.matchers.internal

import com.wix.hoopoe.http.matchers.ResponseMatchers._
import com.wix.hoopoe.http.matchers.drivers.HttpResponseFactory._
import com.wix.hoopoe.http.matchers.drivers.{HttpResponseTestSupport, MatchersTestSupport}
import com.wix.hoopoe.http.matchers.json.Marshaller
import com.wix.hoopoe.http.matchers.json.MarshallingTestObjects.SomeCaseClass
import org.specs2.matcher.ThrownExpectations
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class ResponseBodyMatchersTest extends SpecWithJUnit with MatchersTestSupport {

  trait ctx extends Scope with HttpResponseTestSupport with Mockito with ThrownExpectations


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
      implicit val marshaller = mock[Marshaller]

      marshaller.unmarshall[SomeCaseClass](content) returns someObject

      aResponseWith(content) must havePayloadWith(entity = someObject)
      aResponseWith(content) must not( havePayloadWith(entity = anotherObject) )
    }

    "provide a meaningful explaination why match failed" in new ctx {
      implicit val marshaller = mock[Marshaller]
      marshaller.unmarshall[SomeCaseClass](content) throws new RuntimeException

      failureMessageFor(havePayloadWith(entity = someObject), matchedOn = aResponseWith(content)) must_===
        s"Failed to unmarshall: [$content]"
    }

    "recover gracefully from a badly behaving marshaller" in new ctx {
      implicit val marshaller = mock[Marshaller]
      marshaller.unmarshall[SomeCaseClass](content) returns someObject

      failureMessageFor(havePayloadThat(must = be_===(anotherObject)), matchedOn = aResponseWith(content)) must_===
        s"Failed to match: ['$someObject' is not equal to '$anotherObject'] with content: [$content]"
    }

    "support custom matcher for user object" in new ctx {
      implicit val marshaller = mock[Marshaller]
      marshaller.unmarshall[SomeCaseClass](content) returns someObject

      aResponseWith(content) must havePayloadThat(must = be_===(someObject))
      aResponseWith(content) must not( havePayloadThat(must = be_===(anotherObject)) )
    }

    // add another test for default marshaller
  }
}
