package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.CommonTestMatchers._
import com.wix.e2e.http.matchers.drivers.HttpRequestFactory._
import com.wix.e2e.http.matchers.drivers.{HttpMessageTestSupport, MatchersTestSupport}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec


class RequestUrlMatchersTest extends AnyWordSpec with MatchersTestSupport {

  trait ctx extends HttpMessageTestSupport

  "RequestUrlMatchers" should {

    "match exact path" in new ctx {
      aRequestWithPath(somePath) should havePath(somePath)
      aRequestWithPath(somePath) should not( havePath(anotherPath) )
    }

    "match exact path matcher" in new ctx {
      aRequestWithPath(somePath) should havePathThat(must = be( somePath ))
      aRequestWithPath(somePath) should not( havePathThat(must = be( anotherPath )) )
    }
    // if first ignore first slash ???

    "contain parameter will check if any parameter is present" in new ctx {
      aRequestWithParameters(parameter, anotherParameter) should haveAnyParamOf(parameter)
      aRequestWithParameters(parameter) should not( haveAnyParamOf(anotherParameter) )
    }

    "return detailed message on hasAnyOf match failure" in new ctx {
      failureMessageFor(haveAnyParamOf(parameter, anotherParameter), matchedOn = aRequestWithParameters(yetAnotherParameter, andAnotherParameter)) shouldBe
        s"Could not find parameter [${parameter._1}, ${anotherParameter._1}] but found those: [${yetAnotherParameter._1}, ${andAnotherParameter._1}]"
    }

    "contain parameter will check if all parameters are present" in new ctx {
      aRequestWithParameters(parameter, anotherParameter, yetAnotherParameter) should haveAllParamFrom(parameter, anotherParameter)
      aRequestWithParameters(parameter, yetAnotherParameter) should not( haveAllParamFrom(parameter, anotherParameter) )
    }

    "allOf matcher will return a message stating what was found, and what is missing from parameter list" in new ctx {
      failureMessageFor(haveAllParamFrom(parameter, anotherParameter), matchedOn = aRequestWithParameters(parameter, yetAnotherParameter)) shouldBe
        s"Could not find parameter [${anotherParameter._1}] but found those: [${parameter._1}]."
    }

    "same parameter as will check if the same parameters is present" in new ctx {
      aRequestWithParameters(parameter, anotherParameter) should haveTheSameParamsAs(parameter, anotherParameter)
      aRequestWithParameters(parameter, anotherParameter) should not( haveTheSameParamsAs(parameter) )
      aRequestWithParameters(parameter) should not( haveTheSameParamsAs(parameter, anotherParameter) )
    }

    "haveTheSameParametersAs matcher will return a message stating what was found, and what is missing from parameter list" in new ctx {
      failureMessageFor(haveTheSameParamsAs(parameter, anotherParameter), matchedOn = aRequestWithParameters(parameter, yetAnotherParameter)) shouldBe
        s"Request parameters are not identical, missing parameters from request: [${anotherParameter._1}], request contained extra parameters: [${yetAnotherParameter._1}]."
    }

    "request with no parameters will show a 'no parameters' message" in new ctx {
      failureMessageFor(haveAnyParamOf(parameter), matchedOn = aRequestWithNoParameters ) shouldBe
        "Request did not contain any request parameters."

      failureMessageFor(haveAllParamFrom(parameter), matchedOn = aRequestWithNoParameters ) shouldBe
        "Request did not contain any request parameters."

      failureMessageFor(haveTheSameParamsAs(parameter), matchedOn = aRequestWithNoParameters ) shouldBe
        "Request did not contain any request parameters."
    }

    "match if any parameter satisfy the composed matcher" in new ctx {
      aRequestWithParameters(parameter) should haveAnyParamThat(must = be(parameter._2), withParamName = parameter._1)
      aRequestWithParameters(parameter) should not( haveAnyParamThat(must = be(anotherParameter._2), withParamName = anotherParameter._1) )
    }

    "return informative error messages" in new ctx {
      failureMessageFor(haveAnyParamThat(must = AlwaysMatcher(), withParamName = nonExistingParamName), matchedOn = aRequestWithParameters(parameter)) shouldBe
        s"Request contain parameter names: [${parameter._1}] which did not contain: [$nonExistingParamName]"
      failureMessageFor(haveAnyParamThat(must = AlwaysMatcher(), withParamName = nonExistingParamName), matchedOn = aRequestWithNoParameters) shouldBe
        "Request did not contain any parameters."
      failureMessageFor(haveAnyParamThat(must = be(anotherParameter._2), withParamName = parameter._1), matchedOn = aRequestWithParameters(parameter)) shouldBe
        s"Request parameter [${parameter._1}], did not match { ${be(anotherParameter._2).apply(parameter._2).failureMessage} }"
    }
  }
}