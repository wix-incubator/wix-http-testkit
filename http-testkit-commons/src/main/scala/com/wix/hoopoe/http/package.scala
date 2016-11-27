package com.wix.hoopoe

import akka.http.scaladsl.{model => akka}

package object http {
  type RequestHandler = PartialFunction[HttpRequest, HttpResponse]
  type RequestTransformer = HttpRequest ⇒ HttpRequest
  type HttpRequest = akka.HttpRequest
  type HttpResponse = akka.HttpResponse
}
