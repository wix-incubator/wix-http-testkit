package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.{HttpHeader, HttpMessage}
import com.wix.e2e.http.matchers.internal.HeaderComparison._
import org.specs2.matcher.{Expectable, MatchResult, Matcher, createExpectable}

object HeaderComparison {

  def compare(expectedHeaders: Seq[(String, String)], actualHeaders: Seq[(String, String)]): HeaderComparisonResult = {
    val identical = expectedHeaders & actualHeaders
    val baseMissing = expectedHeaders - identical
    val baseExtra = actualHeaders - identical

    val contentDiff = baseMissing intersectWithSameName baseExtra
    val missing = baseMissing.removeNames(contentDiff)
    val extra = baseExtra.removeNames(contentDiff)

    HeaderComparisonResult(identical, missing, extra, contentDiff)
  }

  implicit class `string tuple as header`(header: (String, _)) {
    def name: String = header._1
    def value: String = header._2.toString

    def sameNameAs(another: (String, _)): Boolean = header.name.equalsIgnoreCase(another.name)

    def sameAs(another: (String, String)): Boolean = header.sameNameAs(another) && header.value == another.value
  }

  implicit class `operations for header sequences`(headerSeq: Seq[(String, String)]) {
    def names: String = headerSeq.map(_.name).mkString(", ")

    def &(another: Seq[(String, String)]): Seq[(String, String)] = headerSeq.filter(h => another.exists(h.sameAs))

    def -(another: Seq[(String, String)]): Seq[(String, String)] = headerSeq.filter(h => !another.exists(h.sameAs))

    def intersectWithSameName(another: Seq[(String, String)]): Seq[(String, (String, String))] =
      for (header <- headerSeq;
           anotherHeader <- another
           if header.sameNameAs(anotherHeader))
      yield (header.name, (header.value, anotherHeader.value))

    def removeNames(another: Seq[(String, _)]): Seq[(String, String)] = headerSeq.filterNot(h => another.exists(h.sameNameAs))

  }

  case class HeaderComparisonResult(identical: Seq[(String, String)],
                                    missing: Seq[(String, String)],
                                    extra: Seq[(String, String)],
                                    contentDiff: Seq[(String, (String, String))]) {

    def formatDiff: String =
      (for ((name, (value1, value2)) <- contentDiff) yield (s"$name -> ($value1 != $value2)")).mkString(", ")
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
    haveHeaderInternal(headers,
      { case res if res.identical.isEmpty => s"Could not find header [${res.missing.names}] but found those: [${res.extra.names}]" })

  def haveAllHeadersOf(headers: (String, String)*): Matcher[T] =
    haveHeaderInternal(headers,
      { case HeaderComparisonResult(identical, missing, extra, _) if missing.nonEmpty => s"Could not find header [${missing.names}] but found those: [${identical.names}]." },
      { case res if res.contentDiff.nonEmpty => s"found headers with different content: [${res.formatDiff}]." })

  def haveTheSameHeadersAs(headers: (String, String)*): Matcher[T] =
    haveHeaderInternal(headers,
      { case HeaderComparisonResult(_, missing, extra, _) if missing.nonEmpty || extra.nonEmpty =>
        s"${httpMessageType.name} header is not identical, missing headers from ${httpMessageType.lowerCaseName}: [${missing.names}], ${httpMessageType.lowerCaseName} contained extra headers: [${extra.names}]."
      },
      { case res if res.contentDiff.nonEmpty => s"found headers with different content: [${res.formatDiff}]." }
    )


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
                                 failureDetectors: PartialFunction[HeaderComparisonResult, String]*): Matcher[T] = new Matcher[T] {

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

      val result = failureDetectors.flatMap(_.lift(comparisonResult))
      val formattedResult = result.mkString(" Also ").capitalize

      if (result.isEmpty) success("ok", t)
      else if (actualHeaders.isEmpty) failure(s"${httpMessageType.name} did not contain any headers.", t)
      else failure(formattedResult, t)
    }
  }
}
