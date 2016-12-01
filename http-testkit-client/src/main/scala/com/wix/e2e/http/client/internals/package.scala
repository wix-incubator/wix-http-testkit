package com.wix.e2e.http.client

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Path
import com.wix.e2e.http.BaseUri

package object internals {

  implicit class `BaseUri --> akka.Uri`(private val u: BaseUri) extends AnyVal {
    def asUriWith(path: String) =
      Uri(scheme = "http").withHost(u.host)
                          .withPort(u.port)
                          .withPath(basePath + path )

    private def basePath =
      u.contextRoot.map( Path(_) )
       .getOrElse( Path.SingleSlash )
  }
}