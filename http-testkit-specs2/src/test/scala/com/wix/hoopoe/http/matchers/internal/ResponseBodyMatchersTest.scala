package com.wix.hoopoe.http.matchers.internal

import com.wix.hoopoe.http.matchers.ResponseMatchers._
import com.wix.hoopoe.http.matchers.drivers.HttpResponseFactory._
import com.wix.hoopoe.http.matchers.drivers.HttpResponseTestSupport
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class ResponseBodyMatchersTest extends SpecWithJUnit {

  trait ctx extends Scope with HttpResponseTestSupport


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
  }
}

//
//    "match body entity from json" in new ctx {
//      HttpResponse(entity = HttpEntity(entity.asJsonStr)) must haveBody(entity = entity)
//      HttpResponse(entity = HttpEntity(entity.asJsonStr)) must not(haveBody(entity = entity.copy(str = randomContent)))
//    }
//
//    "match body entity from json with custom matcher" in new ctx {
//      HttpResponse(entity = HttpEntity(entity.asJsonStr)) must haveBody(entityThatIs = typedEqualTo(entity))
//      HttpResponse(entity = HttpEntity(entity.asJsonStr)) must not(haveBody[SomeEntity](entityThatIs = be_!=(entity)))
//    }
