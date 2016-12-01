package com.wix.e2e.http

case class BaseUri(host: String = "localhost", port: Int, contextRoot: Option[String] = None)
