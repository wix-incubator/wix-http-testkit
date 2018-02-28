package com.wix.e2e

import akka.http.scaladsl.{model => akka}

package object http {
  type RequestHandler = PartialFunction[HttpRequest, HttpResponse]
  type RequestTransformer = HttpRequest ⇒ HttpRequest
  type RequestFilter = HttpRequest ⇒ Boolean
  type HttpRequest = akka.HttpRequest
  type HttpResponse = akka.HttpResponse
}
