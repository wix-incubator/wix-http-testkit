package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.exceptions.MarshallerErrorException
import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.HttpRequestFactory._
import com.wix.e2e.http.matchers.drivers.MarshallingTestObjects.SomeCaseClass
import com.wix.e2e.http.matchers.drivers.{CustomMarshallerProvider, HttpMessageTestSupport, MarshallerTestSupport, MatchersTestSupport}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec


class RequestBodyMatchersTest extends AnyWordSpec with MatchersTestSupport {

  trait ctx extends HttpMessageTestSupport with MarshallerTestSupport with CustomMarshallerProvider

  "ResponseBodyMatchers" should {

    "exact match on response body" in new ctx {
      aRequestWith(content) should haveBodyWith(content)
      aRequestWith(content) should not( haveBodyWith(anotherContent) )
    }

    "match underlying matcher with body content" in new ctx {
      aRequestWith(content) should haveBodyThat(must = be( content ))
      aRequestWith(content) should not( haveBodyThat(must = be( anotherContent )) )
    }

    "exact match on response binary body" in new ctx {
      aRequestWith(binaryContent) should haveBodyWith(binaryContent)
      aRequestWith(binaryContent) should not( haveBodyWith(anotherBinaryContent) )
    }

    "match underlying matcher with binary body content" in new ctx {
      aRequestWith(binaryContent) should haveBodyDataThat(must = be( binaryContent ))
      aRequestWith(binaryContent) should not( haveBodyDataThat(must = be( anotherBinaryContent )) )
    }

    "handle empty body" in new ctx {
      aRequestWithoutBody should not( haveBodyWith(content))
    }

    "support unmarshalling body content with user custom unmarshaller" in new ctx {
      givenUnmarshallerWith[SomeCaseClass](someObject, forContent = content)

      aRequestWith(content) should haveBodyWith(entity = someObject)
      aRequestWith(content) should not( haveBodyWith(entity = anotherObject) )
    }

    "provide a meaningful explanation why match failed" in new ctx {
      givenUnmarshallerWith[SomeCaseClass](someObject, forContent = content)

      failureMessageFor(haveBodyEntityThat(must = be(anotherObject)), matchedOn = aRequestWith(content)) shouldBe
        s"Failed to match: ['$someObject' != '$anotherObject'] with content: ['$content']"
      failureMessageFor(not(haveBodyEntityThat(must = be(anotherObject))), matchedOn = aRequestWith(content)) shouldBe
        s"Failed to match: ['$someObject'] was not equal to ['$anotherObject'] for content: ['$content']"
      failureMessageFor(not( haveBodyEntityThat(must = be(someObject))), matchedOn = aRequestWith(content)) shouldBe
        s"Failed to match: ['$someObject'] was equal to content: ['$content']"
    }

    "provide a proper message to user sent a matcher to an entity matcher" in new ctx {
      failureMessageFor(haveBodyWith(entity = be(someObject)), matchedOn = aRequestWith(content)) shouldBe
        "Matcher misuse: `haveBodyWith` received a matcher to match against, please use `haveBodyThat` instead."
      failureMessageFor(not( haveBodyWith(entity = be(someObject)) ), matchedOn = aRequestWith(content)) shouldBe
        "Matcher misuse: `haveBodyWith` received a matcher to match against, please use `haveBodyThat` instead."
    }

    "provide a proper message to user in case of a badly behaving marshaller" in new ctx {
      givenBadlyBehavingUnmarshallerFor[SomeCaseClass](withContent = content)

      the [MarshallerErrorException] thrownBy haveBodyWith(entity = someObject).apply( aRequestWith(content) )
    }

    "support custom matcher for user object" in new ctx {
      givenUnmarshallerWith[SomeCaseClass](someObject, forContent = content)

      aRequestWith(content) should haveBodyEntityThat(must = be(someObject))
      aRequestWith(content) should not( haveBodyEntityThat(must = be(anotherObject)) )
    }
  }
}

