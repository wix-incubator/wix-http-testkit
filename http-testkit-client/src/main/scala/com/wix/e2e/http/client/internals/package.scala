package com.wix.e2e.http.client

import java.net.URLEncoder

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Path
import com.wix.e2e.http.BaseUri

package object internals {

  implicit class `BaseUri --> akka.Uri`(private val u: BaseUri) extends AnyVal {
    def asUri: Uri = asUriWith("")
    def asUriWith(relativeUrl: String): Uri =
      if (relativeUrl.contains('?'))
        urlWithoutParams(relativeUrl).withRawQueryString( extractParamsFrom(relativeUrl) )
      else urlWithoutParams(relativeUrl)


    private def fixPath(url: Option[String]) = {
      url.map( _.trim )
         .map( u => s"/${u.stripPrefix("/")}" )
         .filterNot( _.equals("/") )
         .map( Path(_) )
         .getOrElse( Path.Empty )
    }

    private def buildPath(context: Option[String], relativePath: Option[String]) = {
      val c = fixPath(context)
      val r = fixPath(relativePath)
      c ++ r
    }

    private def urlWithoutParams(relativeUrl: String) =
      Uri(scheme = "http").withHost(u.host)
                          .withPort(u.port)
                          .withPath( buildPath(u.contextRoot, Option(extractPathFrom(relativeUrl))) )

    private def extractPathFrom(relativeUrl: String) = relativeUrl.split('?').head

    private def extractParamsFrom(relativeUrl: String) =
      rebuildAndEscapeParams(relativeUrl.substring(relativeUrl.indexOf('?') + 1))

    private def rebuildAndEscapeParams(p: String) =
      p.split("&")
       .map( _.split("=") )
       .map( { case Array(k, v) => s"$k=${URLEncoder.encode(v, "UTF-8")}"
          case Array(k) => k
       } )
       .mkString("&")
  }
}