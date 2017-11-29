package com.wix.e2e.http.marshaller

import com.wix.e2e.http.api.Marshaller.Implicits._
import com.wix.e2e.http.client.extractors.HttpMessageExtractors
import com.wix.e2e.http.client.transformers.HttpClientTransformers
import com.wix.e2e.http.drivers.MarshallerMatchers._
import com.wix.e2e.http.drivers.MarshallerTestSupport
import com.wix.e2e.http.drivers.MarshallingTestObjects.SomeCaseClass
import com.wix.e2e.http.exceptions.MissingMarshallerException
import com.wix.e2e.http.matchers.{RequestMatchers, ResponseMatchers}
import org.specs2.mutable.Spec
import org.specs2.specification.Scope

class HttpClientNoCustomMarshallerContractTest extends Spec with HttpClientTransformers with HttpMessageExtractors {

  trait ctx extends Scope with MarshallerTestSupport

  "Response Transformers without custom marshaller" should {

    "detect custom marshaller and use it to marshall body payload" in new ctx {
      withPayload(someObject).apply(request) must throwA[MissingMarshallerException]
    }


    "print informative error message when marshaller is not included" in new ctx {
      aResponseWith(content).extractAs[SomeCaseClass] must throwA[MissingMarshallerException]
    }
  }

  "RequestBodyMatchers without custom marshaller" should {

    "print informative error message when marshaller is not included" in new ctx {
      RequestMatchers.haveBodyWith(entity = someObject).apply(aRequestWith(content)) must beMissingMarshallerMatcherError
    }

    "print informative error message when marshaller is not included2" in new ctx {
      RequestMatchers.haveBodyEntityThat(must = be_===(someObject)).apply(aRequestWith(content)) must beMissingMarshallerMatcherError
    }
  }

  "ResponseBodyMatchers without custom marshaller" should {
    "in haveBodyWith matcher, print informative error message when marshaller is not included" in new ctx {
      ResponseMatchers.haveBodyWith(entity = someObject).apply(aResponseWith(content)) must beMissingMarshallerMatcherError
    }

    "in haveBodyThat matcher, print informative error message when marshaller is not included2" in new ctx {
      ResponseMatchers.haveBodyThat(must = be_===(someObject)).apply(aResponseWith(content)) must beMissingMarshallerMatcherError
    }
  }
}
