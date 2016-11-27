package com.wix.hoopoe

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

package object http {
  type RequestHandler = PartialFunction[HttpRequest, HttpResponse]
}
