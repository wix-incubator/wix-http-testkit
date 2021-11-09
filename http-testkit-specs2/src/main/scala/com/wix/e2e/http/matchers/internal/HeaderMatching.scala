package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.{HttpHeader, HttpMessage}
import org.specs2.matcher.{Expectable, MatchResult, Matcher, createExpectable}

case class HeaderComparisonResult(identical: Seq[(String, String)], missing: Seq[(String, String)], extra: Seq[(String, String)])

object HeaderComparison {

  private def compareHeader(header1: (String, String), header2: (String, String)) = header1._1.toLowerCase == header2._1.toLowerCase && header1._2 == header2._2

  def compare(expectedHeaders: Seq[(String, String)], actualHeaders: Seq[(String, String)]): HeaderComparisonResult = {
    val identical = expectedHeaders.filter(h1 => actualHeaders.exists(h2 => compareHeader(h1, h2)))
    val missing = expectedHeaders.filter(h1 => !identical.exists(h2 => compareHeader(h1, h2)))
    val extra = actualHeaders.filter(h1 => !identical.exists(h2 => compareHeader(h1, h2)))

    HeaderComparisonResult(identical, missing, extra)
  }

}

abstract class HttpMessageType(val name: String) {
  def lowerCaseName: String = name.toLowerCase
  def isCookieHeader(header: HttpHeader): Boolean
}


trait HeaderMatching[T <: HttpMessage] {
  protected def httpMessageType: HttpMessageType

  protected def specialHeaders: Map[String, String] = Map.empty

  def haveAnyHeadersOf(headers: (String, String)*): Matcher[T] =
    haveHeaderInternal(headers, _.identical.nonEmpty,
      res => s"Could not find header [${res.missing.map(_._1).mkString(", ")}] but found those: [${res.extra.map(_._1).mkString(", ")}]")

  def haveAllHeadersOf(headers: (String, String)*): Matcher[T] =
    haveHeaderInternal(headers, _.missing.isEmpty,
      res => s"Could not find header [${res.missing.map(_._1).mkString(", ")}] but found those: [${res.identical.map(_._1).mkString(", ")}].")

  def haveTheSameHeadersAs(headers: (String, String)*): Matcher[T] =
    haveHeaderInternal(headers, r => r.extra.isEmpty && r.missing.isEmpty,
      res => s"${httpMessageType.name} header is not identical, missing headers from ${httpMessageType.lowerCaseName}: [${res.missing.map(_._1).mkString(", ")}], ${httpMessageType.lowerCaseName} contained extra headers: [${res.extra.map(_._1).mkString(", ")}].")


  def haveAnyHeaderThat(must: Matcher[String], withHeaderName: String): Matcher[T] = new Matcher[T] {
    def apply[S <: T](t: Expectable[S]): MatchResult[S] = {
      val actual = t.value
      val actualHeaders = actual.headers.filterNot(httpMessageType.isCookieHeader)
      val foundHeader = actualHeaders.find(_.name.equalsIgnoreCase(withHeaderName)).map(_.value)

      foundHeader match {
        case None if actualHeaders.isEmpty => failure(s"${httpMessageType.name} did not contain any headers.", t)
        case None => failure(s"${httpMessageType.name} contain header names: [${actualHeaders.map(_.name).mkString(", ")}] which did not contain: [$withHeaderName]", t)
        case Some(value) if must.apply(createExpectable(value)).isSuccess => success("ok", t)
        case Some(value) => failure(s"${httpMessageType.name} header [$withHeaderName], did not match { ${must.apply(createExpectable(value)).message} }", t)
      }
    }
  }

  private def haveHeaderInternal(expectedHeaders: Seq[(String, String)],
                                 comparator: HeaderComparisonResult => Boolean,
                                 errorMessage: HeaderComparisonResult => String): Matcher[T] = new Matcher[T] {

    def apply[S <: T](t: Expectable[S]): MatchResult[S] =
      checkSpecialHeaders(t).getOrElse(buildHeaderMatchResult(t))

    private def checkSpecialHeaders[S <: T](t: Expectable[S]) =
      expectedHeaders.map(h => h._1.toLowerCase)
        .collectFirst { case name if specialHeaders.contains(name) => failure(specialHeaders(name), t) }

    private def buildHeaderMatchResult[S <: T](t: Expectable[S]) = {
      val actual = t.value
      val actualHeaders = actual.headers
        .filterNot(httpMessageType.isCookieHeader)
        .map(h => h.name -> h.value)

      val comparisonResult = HeaderComparison.compare(expectedHeaders, actualHeaders)

      if (comparator(comparisonResult)) success("ok", t)
      else if (actualHeaders.isEmpty) failure(s"${httpMessageType.name} did not contain any headers.", t)
      else failure(errorMessage(comparisonResult), t)
    }
  }
}
