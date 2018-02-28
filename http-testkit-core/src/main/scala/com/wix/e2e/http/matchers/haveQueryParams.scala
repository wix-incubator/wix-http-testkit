package com.wix.e2e.http.matchers

import com.wix.e2e.http.RequestMatcher

object haveQueryParams {

  def apply(param: (String, String), params: (String, String)*): RequestMatcher = { rq =>
    val requestedQueryParams = rq.uri.query().toMap
    val expectedQueryParams = (param +: params).toMap

    expectedQueryParams forall { case (k, v) => requestedQueryParams.get(k).contains(v) }
  }
}
