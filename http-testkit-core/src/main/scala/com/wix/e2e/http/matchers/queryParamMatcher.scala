package com.wix.e2e.http.matchers

import com.wix.e2e.http.RequestMatcher

object queryParamMatcher {

  def apply(params: Seq[(String, String)]): RequestMatcher = { rq =>
    val requestedQueryParams = rq.uri.query().toMap
    val expectedQueryParams = params.toMap

    expectedQueryParams forall { case (k, v) => requestedQueryParams.get(k).contains(v) }
  }
}
