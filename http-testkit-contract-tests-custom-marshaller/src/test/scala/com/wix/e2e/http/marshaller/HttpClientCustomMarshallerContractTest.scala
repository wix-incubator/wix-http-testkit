package com.wix.e2e.http.marshaller

import com.wix.e2e.http.api.Marshaller.Implicits._
import com.wix.e2e.http.client.transformers.HttpClientTransformers
import com.wix.e2e.http.drivers.MarshallerTestSupport
import com.wix.e2e.http.drivers.MarshallingTestObjects.SomeCaseClass
import com.wix.e2e.http.matchers.{RequestMatchers, ResponseMatchers}
import org.specs2.mutable.Spec
import org.specs2.specification.Scope

class HttpClientCustomMarshallerContractTest extends Spec with HttpClientTransformers {

  trait ctx extends Scope with MarshallerTestSupport

  "RequestTransformers with custom marshaller" should {

    "detect custom marshaller and use it to marshall body payload" in new ctx {
      givenMarshallerThatUnmarshalWith(someObject, content)
      givenMarshallerThatMarshal(content, to = someObject)

      withPayload(someObject).apply(request) must RequestMatchers.haveBodyWith(someObject)
    }

    "detect custom marshaller and use it to extract response" in new ctx {
      givenMarshallerThatUnmarshalWith(someObject, content)

      aResponseWith(content).extractAs[SomeCaseClass] must_=== someObject
    }
  }

  "RequestBodyMatchers with custom marshaller" should {

    "in haveBodyWith, support unmarshalling body content with user custom unmarshaller" in new ctx {
      givenMarshallerThatUnmarshalWith(someObject, forContent = content)

      aRequestWith(content) must RequestMatchers.haveBodyWith(entity = someObject)
    }

    "in haveBodyEntityThat, support unmarshalling body content with user custom unmarshaller" in new ctx {
      givenMarshallerThatUnmarshalWith(someObject, forContent = content)

      aRequestWith(content) must RequestMatchers.haveBodyEntityThat(must = be_===( someObject ))
    }
  }

  "ResponseBodyMatchers with custom marshaller" should {

    "in haveBodyWith matcher, detect custom marshaller from classpath and use it to unmarshal request" in new ctx {
      givenMarshallerThatUnmarshalWith(someObject, forContent = content)

      aResponseWith(content) must ResponseMatchers.haveBodyWith(someObject)
    }

    "in haveBodyThat matcher, detect custom marshaller from classpath and use it to unmarshal request" in new ctx {
      givenMarshallerThatUnmarshalWith(someObject, forContent = content)

      aResponseWith(content) must ResponseMatchers.haveBodyThat(must = be_===(someObject))
    }

    "in beSuccessfulWith matcher, detect custom marshaller from classpath and use it to unmarshal request" in new ctx {
      givenMarshallerThatUnmarshalWith(someObject, forContent = content)

      aResponseWith(content) must ResponseMatchers.beSuccessfulWith(someObject)
    }

    "in beSuccessfulWithEntityThat matcher, detect custom marshaller from classpath and use it to unmarshal request" in new ctx {
      givenMarshallerThatUnmarshalWith(someObject, forContent = content)

      aResponseWith(content) must ResponseMatchers.beSuccessfulWithEntityThat(must = be_===(someObject))
    }
  }
}
