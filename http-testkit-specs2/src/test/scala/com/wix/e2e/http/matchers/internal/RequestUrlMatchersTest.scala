package com.wix.e2e.http.matchers.internal

import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.HttpRequestFactory._
import com.wix.e2e.http.matchers.drivers.MatchersTestSupport
import com.wix.test.random._
import org.specs2.matcher.AlwaysMatcher
import org.specs2.mutable.Spec
import org.specs2.specification.Scope


class RequestUrlMatchersTest extends Spec with MatchersTestSupport {

  trait ctx extends Scope {

    val somePath = randomPath
    val anotherPath = randomPath

    val parameter = randomParameter
    val anotherParameter = randomParameter
    val yetAnotherParameter = randomParameter
    val andAnotherParameter = randomParameter

    val nonExistingParamName = randomStr

    private def randomPath = "/" + Seq.fill(5)(randomStr).mkString("/")
    private def randomParameter = randomStr -> randomStr
  }


  "RequestUrlMatchers" should {

    "match exact path" in new ctx {
      aRequestWithPath(somePath) must havePath(somePath)
      aRequestWithPath(somePath) must not( havePath(anotherPath) )
    }

    "match exact path matcher" in new ctx {
      aRequestWithPath(somePath) must havePathThat(must = be_===( somePath ))
      aRequestWithPath(somePath) must not( havePathThat(must = be_===( anotherPath )) )
    }
    // if first ignore first slash ???

    "contain parameter will check if any parameter is present" in new ctx {
      aRequestWithParameters(parameter, anotherParameter) must haveAnyParamOf(parameter)
      aRequestWithParameters(parameter) must not( haveAnyParamOf(anotherParameter) )
    }

    "return detailed message on hasAnyOf match failure" in new ctx {
      failureMessageFor(haveAnyParamOf(parameter, anotherParameter), matchedOn = aRequestWithParameters(yetAnotherParameter, andAnotherParameter)) must_===
        s"Could not find parameter [${parameter._1}, ${anotherParameter._1}] but found those: [${yetAnotherParameter._1}, ${andAnotherParameter._1}]"
    }

    "contain parameter will check if all parameters are present" in new ctx {
      aRequestWithParameters(parameter, anotherParameter, yetAnotherParameter) must haveAllParamFrom(parameter, anotherParameter)
      aRequestWithParameters(parameter, yetAnotherParameter) must not( haveAllParamFrom(parameter, anotherParameter) )
    }

    "allOf matcher will return a message stating what was found, and what is missing from parameter list" in new ctx {
      failureMessageFor(haveAllParamFrom(parameter, anotherParameter), matchedOn = aRequestWithParameters(parameter, yetAnotherParameter)) must_===
        s"Could not find parameter [${anotherParameter._1}] but found those: [${parameter._1}]."
    }

    "same parameter as will check if the same parameters is present" in new ctx {
      aRequestWithParameters(parameter, anotherParameter) must haveTheSameParamsAs(parameter, anotherParameter)
      aRequestWithParameters(parameter, anotherParameter) must not( haveTheSameParamsAs(parameter) )
      aRequestWithParameters(parameter) must not( haveTheSameParamsAs(parameter, anotherParameter) )
    }

    "haveTheSameParametersAs matcher will return a message stating what was found, and what is missing from parameter list" in new ctx {
      failureMessageFor(haveTheSameParamsAs(parameter, anotherParameter), matchedOn = aRequestWithParameters(parameter, yetAnotherParameter)) must_===
        s"Request parameters are not identical, missing parameters from request: [${anotherParameter._1}], request contained extra parameters: [${yetAnotherParameter._1}]."
    }

    "request with no parameters will show a 'no parameters' message" in new ctx {
      failureMessageFor(haveAnyParamOf(parameter), matchedOn = aRequestWithNoParameters ) must_===
        "Request did not contain any request parameters."

      failureMessageFor(haveAllParamFrom(parameter), matchedOn = aRequestWithNoParameters ) must_===
        "Request did not contain any request parameters."

      failureMessageFor(haveTheSameParamsAs(parameter), matchedOn = aRequestWithNoParameters ) must_===
        "Request did not contain any request parameters."
    }

    "match if any parameter satisfy the composed matcher" in new ctx {
      aRequestWithParameters(parameter) must haveAnyParamThat(must = be_===(parameter._2), withParamName = parameter._1)
      aRequestWithParameters(parameter) must not( haveAnyParamThat(must = be_===(anotherParameter._2), withParamName = anotherParameter._1) )
    }

    "return informative error messages" in new ctx {
      failureMessageFor(haveAnyParamThat(must = AlwaysMatcher(), withParamName = nonExistingParamName), matchedOn = aRequestWithParameters(parameter)) must_===
        s"Request contain parameter names: [${parameter._1}] which did not contain: [$nonExistingParamName]"
      failureMessageFor(haveAnyParamThat(must = AlwaysMatcher(), withParamName = nonExistingParamName), matchedOn = aRequestWithNoParameters) must_===
        "Request did not contain any parameters."
      failureMessageFor(haveAnyParamThat(must = be_===(anotherParameter._2), withParamName = parameter._1), matchedOn = aRequestWithParameters(parameter)) must_===
        s"Request parameter [${parameter._1}], did not match { ${be_===(anotherParameter._2).apply(parameter._2).message} }"
    }
  }
}