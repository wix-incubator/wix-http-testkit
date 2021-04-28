package com.wix.e2e.http.config

import scala.concurrent.duration._
import scala.util.Try

object Config {
  private val DefaultTimeoutConfig = "wix.config.default-timeout"

  val DefaultTimeout: FiniteDuration = read( DefaultTimeoutConfig ).getOrElse(5.seconds)

  private def read(property: String) = Option(System.getProperty(property)).flatMap( parse )

  private def parse(timeoutStr: String) = Try( timeoutStr.toLong.seconds ).toOption
}
