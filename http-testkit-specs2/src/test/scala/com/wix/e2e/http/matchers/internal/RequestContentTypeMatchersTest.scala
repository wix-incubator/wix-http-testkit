package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.ContentTypes._
import com.wix.e2e.http.matchers.RequestMatchers._
import com.wix.e2e.http.matchers.drivers.HttpRequestFactory._
import com.wix.e2e.http.matchers.drivers.{HttpResponseTestSupport, MatchersTestSupport}
import org.specs2.mutable.Spec
import org.specs2.specification.Scope

class RequestContentTypeMatchersTest extends Spec with MatchersTestSupport {

  trait ctx extends Scope with HttpResponseTestSupport

  "RequestContentTypeMatchers" should {

    "exact match on request json content type" in new ctx {
      aRequestWith(`application/json`) must haveJsonBody
      aRequestWith(`text/csv(UTF-8)`) must not( haveJsonBody )
    }

    "exact match on request text plain content type" in new ctx {
      aRequestWith(`text/plain(UTF-8)`) must haveTextPlainBody
      aRequestWith(`text/csv(UTF-8)`) must not( haveTextPlainBody )
    }

    "exact match on request form url encoded content type" in new ctx {
      aRequestWith(`application/x-www-form-urlencoded`) must haveFormUrlEncodedBody
      aRequestWith(`text/csv(UTF-8)`) must not( haveFormUrlEncodedBody )
    }

    "exact match on multipart request content type" in new ctx {
      aRequestWith(`multipart/form-data`) must haveMultipartFormBody
      aRequestWith(`text/csv(UTF-8)`) must not( haveMultipartFormBody )
    }
  }
}
