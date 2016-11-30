package com.wix.hoopoe.http

case class BaseUri(host: String = "localhost", port: Int, contextRoot: Option[String] = None)