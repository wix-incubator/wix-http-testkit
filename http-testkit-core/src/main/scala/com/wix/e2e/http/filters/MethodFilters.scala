package com.wix.e2e.http.filters

import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import com.wix.e2e.http.RequestFilter

trait MethodFilters {

  def whenGet: RequestFilter = whenMethod(HttpMethods.GET)
  def whenPost: RequestFilter = whenMethod(HttpMethods.POST)
  def whenDelete: RequestFilter = whenMethod(HttpMethods.DELETE)
  def whenPut: RequestFilter = whenMethod(HttpMethods.PUT)
  def whenPatch: RequestFilter = whenMethod(HttpMethods.PATCH)

  def whenMethod(method: HttpMethod): RequestFilter = { _.method == method }
}
