package com.wix.e2e.http.filters

import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import com.wix.e2e.http.RequestFilter

trait MethodFilters {

  def forGet: RequestFilter = forMethod(HttpMethods.GET)
  def forPost: RequestFilter = forMethod(HttpMethods.POST)
  def forDelete: RequestFilter = forMethod(HttpMethods.DELETE)
  def forPut: RequestFilter = forMethod(HttpMethods.PUT)
  def forPatch: RequestFilter = forMethod(HttpMethods.PATCH)

  def forMethod(method: HttpMethod): RequestFilter = { _.method == method }
}
