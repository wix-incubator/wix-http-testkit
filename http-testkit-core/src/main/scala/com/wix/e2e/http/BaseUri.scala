package com.wix.e2e.http

import scala.annotation.implicitNotFound

@implicitNotFound(
  """Cannot find system under test host/port.
     Please specify implicit val baseUri: BaseUri parameter.""")
case class BaseUri(host: String = "localhost", port: Int, contextRoot: Option[String] = None)
