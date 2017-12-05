package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.exceptions.MarshallerErrorException
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.MarshallingTestObjects.SomeCaseClass
import com.wix.e2e.http.matchers.drivers.{HttpResponseTestSupport, MarshallerTestSupport, MatchersTestSupport}
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class ResponseBodyMatchersTest extends WordSpec with MatchersTestSupport {

  trait ctx extends HttpResponseTestSupport with MarshallerTestSupport //with CustomMarshallerProvider

  "ResponseBodyMatchers" should {

    "exact match on response body" in new ctx {
      aResponseWith(content) should haveBodyWith(content)
      aResponseWith(content) should not( haveBodyWith(anotherContent) )
    }

    "match underlying matcher with body content" in new ctx {
      aResponseWith(content) should haveBodyThat(must = be( content ))
      aResponseWith(content) should not( haveBodyThat(must = be( anotherContent )) )
    }

    "exact match on response binary body" in new ctx {
      aResponseWith(binaryContent) should haveBodyWith(binaryContent)
      aResponseWith(binaryContent) should not( haveBodyWith(anotherBinaryContent) )
    }

    "match underlying matcher with binary body content" in new ctx {
      aResponseWith(binaryContent) should haveBodyDataThat(must = be( binaryContent ))
      aResponseWith(binaryContent) should not( haveBodyDataThat(must = be( anotherBinaryContent )) )
    }

    "handle empty body" in new ctx {
      aResponseWithoutBody should not( haveBodyWith(content))
    }

    "support unmarshalling body content with user custom unmarshaller" in new ctx {
      implicit val marshaller = givenUnmarshallerWith[SomeCaseClass](someObject, forContent = content)

      aResponseWith(content) should haveBodyWith(entity = someObject)
      aResponseWith(content) should not( haveBodyWith(entity = anotherObject) )
    }

    "provide a meaningful explanation why match failed" in new ctx {
      implicit val marshaller = givenUnmarshallerWith[SomeCaseClass](someObject, forContent = content)

      failureMessageFor(haveBodyEntityThat(must = be(anotherObject)), matchedOn = aResponseWith(content)) shouldBe
        s"Failed to match: ['$someObject' != '$anotherObject'] with content: [$content]"
    }

    "provide a proper message to user in case of a badly behaving marshaller" in new ctx {
      implicit val marshaller = givenBadlyBehavingUnmarshallerFor[SomeCaseClass](withContent = content)

      the[MarshallerErrorException] thrownBy haveBodyWith(entity = someObject).apply( aResponseWith(content) )
    }

    "provide a proper message to user sent a matcher to an entity matcher" in new ctx {
      failureMessageFor(haveBodyWith(entity = be(someObject)), matchedOn = aResponseWith(content)) shouldBe
        s"Matcher misuse: `haveBodyWith` received a matcher to match against, please use `haveBodyThat` instead."
    }

    "support custom matcher for user object" in new ctx {
      implicit val marshaller = givenUnmarshallerWith[SomeCaseClass](someObject, forContent = content)

      aResponseWith(content) should haveBodyEntityThat(must = be(someObject))
      aResponseWith(content) should not( haveBodyEntityThat(must = be(anotherObject)) )
    }
  }
}
