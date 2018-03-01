package com.wix.e2e.http.filters

import com.wix.e2e.http.RequestFilter

trait QueryParamFilters {

  def forQueryParams(param: (String, String), params: (String, String)*): RequestFilter = { rq =>
    val requestedQueryParams = rq.uri.query().toMap
    val expectedQueryParams = (param +: params).toMap

    expectedQueryParams forall { case (k, v) => requestedQueryParams.get(k).contains(v) }
  }
}
