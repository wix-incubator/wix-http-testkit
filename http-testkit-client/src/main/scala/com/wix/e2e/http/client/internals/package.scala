package com.wix.e2e.http.client

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.{Path, Query}
import com.wix.e2e.http.BaseUri

package object internals {

  implicit class `BaseUri --> akka.Uri`(private val u: BaseUri) extends AnyVal {
    def asUriWith(relativeUrl: String) =
      if (relativeUrl.contains('?'))
        urlWithoutParams(relativeUrl).withQuery( extractParamsFrom(relativeUrl) )
      else urlWithoutParams(relativeUrl)


    private def urlWithoutParams(relativeUrl: String) =
      Uri(scheme = "http").withHost(u.host)
                                    .withPort(u.port)
                                    .withPath(basePath + extractPathFrom(relativeUrl))

    private def basePath =
      u.contextRoot.map( Path(_) )
       .getOrElse( Path.SingleSlash )

    private def extractPathFrom(relativeUrl: String) = relativeUrl.split('?').head

    private def extractParamsFrom(relativeUrl: String) =
      Query(relativeUrl.split('?')
                       .drop(1).last)
  }
}