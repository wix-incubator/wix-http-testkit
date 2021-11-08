package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.{HttpHeader, HttpMessage}
import com.wix.e2e.http.matchers.internal.HeaderComparison._
import org.specs2.matcher.{Expectable, MatchResult, Matcher, createExpectable}

object HeaderComparison {

  def compare(expectedHeaders: Seq[(String, String)], actualHeaders: Seq[(String, String)]): HeaderComparisonResult = {
    val identical = expectedHeaders & actualHeaders
    val missing = expectedHeaders - identical
    val extra = actualHeaders - identical

    val contentDiff = missing intersectWithSameName extra

    HeaderComparisonResult(identical, missing, extra, contentDiff)
  }

  implicit class `string tuple as header`(header: (String, String)) {
    def name: String = header._1
    def value: String = header._2

    def sameNameAs(another: (String, String)): Boolean = header.name.equalsIgnoreCase(another.name)

    def sameAs(another: (String, String)): Boolean = header.sameNameAs(another) && header.value == another.value
  }

  implicit class `operations for header sequences`(headerSeq: Seq[(String, String)]) {
    def names: String = headerSeq.map(_.name).mkString(", ")
    def &(another: Seq[(String, String)]): Seq[(String, String)] = headerSeq.filter(h => another.exists(h.sameAs))
    def -(another: Seq[(String, String)]): Seq[(String, String)] = headerSeq.filter(h => !another.exists(h.sameAs))
    def intersectWithSameName(another: Seq[(String, String)]): Seq[(String, String, String)] =
      for ( header <- headerSeq;
            anotherHeader <- another
            if header.sameNameAs(anotherHeader))
      yield (header.name, header.value, anotherHeader.value)

  }

  case class HeaderComparisonResult(identical: Seq[(String, String)],
                                    missing: Seq[(String, String)],
                                    extra: Seq[(String, String)],
                                    contentDiff: Seq[(String, String, String)])

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
      res => s"Could not find header [${res.missing.names}] but found those: [${res.extra.names}]")

  def haveAllHeadersOf(headers: (String, String)*): Matcher[T] =
    haveHeaderInternal(headers, _.missing.isEmpty,
      res => s"Could not find header [${res.missing.names}] but found those: [${res.identical.names}].")

  def haveTheSameHeadersAs(headers: (String, String)*): Matcher[T] =
    haveHeaderInternal(headers, r => r.extra.isEmpty && r.missing.isEmpty,
      res => s"${httpMessageType.name} header is not identical, missing headers from ${httpMessageType.lowerCaseName}: [${res.missing.names}], ${httpMessageType.lowerCaseName} contained extra headers: [${res.extra.names}].")


  def haveAnyHeaderThat(must: Matcher[String], withHeaderName: String): Matcher[T] = new Matcher[T] {
    def apply[S <: T](t: Expectable[S]): MatchResult[S] = {
      val expectedHeaderName = withHeaderName.toLowerCase

      val actual = t.value
      val actualHeaders = actual.headers.filterNot(httpMessageType.isCookieHeader)
      val foundHeader = actualHeaders.collectFirst { case HttpHeader(`expectedHeaderName`, value) => value }

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
