package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Cookie, HttpCookiePair}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.wix.e2e.http.HttpRequest
import com.wix.e2e.http.api.{Marshaller, RequestRecordSupport}
import com.wix.e2e.http.exceptions.{MarshallerErrorException, MissingMarshallerException}
import com.wix.e2e.http.matchers.RequestMatcher
import com.wix.e2e.http.utils._
import org.scalatest.Matchers._
import org.scalatest.matchers.{MatchResult, Matcher}

import scala.util.control.Exception._

trait RequestMethodMatchers {
  def bePost: RequestMatcher = beRequestWith( POST )
  def beGet: RequestMatcher = beRequestWith( GET )
  def beDelete: RequestMatcher = beRequestWith( DELETE )
  def beHead: RequestMatcher = beRequestWith( HEAD )
  def beOptions: RequestMatcher = beRequestWith( OPTIONS )
  def bePatch: RequestMatcher = beRequestWith( PATCH )
  def bePut: RequestMatcher = beRequestWith( PUT )
  def beTrace: RequestMatcher = beRequestWith( TRACE )
  def beConnect: RequestMatcher = beRequestWith( CONNECT )

  private def beRequestWith(method: HttpMethod): RequestMatcher =
    be( method ) compose { (_: HttpRequest).method }
}

trait RequestUrlMatchers {
  def havePath(path: String): RequestMatcher = havePathThat(must = be( path ) )
  def havePathThat(must: Matcher[String]): RequestMatcher = must compose { (_: HttpRequest).uri.path.toString }

  def haveAnyParamOf(params: (String, String)*): RequestMatcher =
    haveParameterInternal(params, _.identical.nonEmpty,
                          req => s"Could not find parameter [${req.missing.map(_._1).mkString(", ")}] but found those: [${req.extra.map(_._1).mkString(", ")}]")

  def haveAllParamFrom(params: (String, String)*): RequestMatcher =
    haveParameterInternal(params, _.missing.isEmpty,
                          req => s"Could not find parameter [${req.missing.map(_._1).mkString(", ")}] but found those: [${req.identical.map(_._1).mkString(", ")}]." )

  def haveTheSameParamsAs(params: (String, String)*): RequestMatcher =
    haveParameterInternal(params, r => r.extra.isEmpty && r.missing.isEmpty,
                          req => s"Request parameters are not identical, missing parameters from request: [${req.missing.map(_._1).mkString(", ")}], request contained extra parameters: [${req.extra.map(_._1).mkString(", ")}]." )

  private def haveParameterInternal(params: Seq[(String, String)], comparator: ParameterComparisonResult => Boolean, errorMessage: ParameterComparisonResult => String): RequestMatcher = new RequestMatcher {
    def apply(request: HttpRequest): MatchResult = {
      val requestParameters = request.uri.query()
      val comparisonResult = compare(params, requestParameters)

      if ( comparator(comparisonResult) ) MatchResult(matches = true, "ok", "not-ok")
      else if (requestParameters.isEmpty) MatchResult(matches = false, "Request did not contain any request parameters.", "not-ok")
      else MatchResult(matches = false, errorMessage(comparisonResult), "not-ok")
    }

    private def compareParam(param1: (String, String), param2: (String, String)) = param1._1 == param2._1 && param1._2 == param2._2

    private def compare(params: Seq[(String, String)], requestParams: Seq[(String, String)]): ParameterComparisonResult = {
      val identical = params.filter( h1 => requestParams.exists( h2 => compareParam(h1, h2) ) )
      val missing = params.filter( h1 => !identical.exists( h2 => compareParam(h1, h2) ) )
      val extra = requestParams.filter( h1 => !identical.exists( h2 => compareParam(h1, h2) ) )

      ParameterComparisonResult(identical, missing, extra)
    }
  }

  def haveAnyParamThat(must: Matcher[String], withParamName: String): RequestMatcher = new RequestMatcher {
    def apply(request: HttpRequest): MatchResult = {
      val requestParameters = request.uri.query()
      val requestParameter = requestParameters.find( _._1 == withParamName )
                                              .map( _._2 )

      requestParameter match {
        case None if requestParameters.isEmpty => MatchResult(matches = false, "Request did not contain any parameters.", "not-ok")
        case None => MatchResult(matches = false, s"Request contain parameter names: [${requestParameters.map( _._1 ).mkString(", ")}] which did not contain: [$withParamName]", "not-ok")
        case Some(v) if must.apply(v).matches => MatchResult(matches = true, "ok", "not-ok")
        case Some(v) => MatchResult(matches = false, s"Request parameter [$withParamName], did not match { ${must.apply(v).failureMessage} }", "not-ok")
      }

    }
  }

  private case class ParameterComparisonResult(identical: Seq[(String, String)], missing: Seq[(String, String)], extra: Seq[(String, String)])
}

trait RequestHeadersMatchers {

  def haveAnyHeadersOf(headers: (String, String)*): RequestMatcher =
    haveHeaderInternal( headers, _.identical.nonEmpty,
      res => s"Could not find header [${res.missing.map(_._1).mkString(", ")}] but found those: [${res.extra.map(_._1).mkString(", ")}]" )

  def haveAllHeadersOf(headers: (String, String)*): RequestMatcher =
    haveHeaderInternal( headers, _.missing.isEmpty,
      res => s"Could not find header [${res.missing.map(_._1).mkString(", ")}] but found those: [${res.identical.map(_._1).mkString(", ")}]." )

  def haveTheSameHeadersAs(headers: (String, String)*): RequestMatcher =
    haveHeaderInternal( headers, r => r.extra.isEmpty && r.missing.isEmpty,
      res => s"Request header is not identical, missing headers from request: [${res.missing.map(_._1).mkString(", ")}], request contained extra headers: [${res.extra.map(_._1).mkString(", ")}]." )

  private def haveHeaderInternal(headers: Seq[(String, String)], comparator: HeaderComparisonResult => Boolean, errorMessage: HeaderComparisonResult => String): RequestMatcher = new RequestMatcher {


    def apply(request: HttpRequest): MatchResult = {
      val requestHeaders = request.headers
                                  .filterNot( _.isInstanceOf[Cookie] )
                                  .map( h => h.name -> h.value )
      val comparisonResult = compare(headers, requestHeaders)

      if ( comparator(comparisonResult) ) MatchResult(matches = true, "ok", "not-ok")
      else if (requestHeaders.isEmpty) MatchResult(matches = false, "Request did not contain any headers.", "not-ok")
      else MatchResult(matches = false, errorMessage(comparisonResult), "not-ok")
    }

    private def compareHeader(header1: (String, String), header2: (String, String)) = header1._1.toLowerCase == header2._1.toLowerCase && header1._2 == header2._2

    private def compare(headers: Seq[(String, String)], requestHeaders: Seq[(String, String)]): HeaderComparisonResult = {
      val identical = headers.filter( h1 => requestHeaders.exists( h2 => compareHeader(h1, h2) ) )
      val missing = headers.filter( h1 => !identical.exists( h2 => compareHeader(h1, h2) ) )
      val extra = requestHeaders.filter( h1 => !identical.exists( h2 => compareHeader(h1, h2) ) )

      HeaderComparisonResult(identical, missing, extra)
    }
  }

  def haveAnyHeaderThat(must: Matcher[String], withHeaderName: String): RequestMatcher = new RequestMatcher {

    def apply(request: HttpRequest): MatchResult = {
      val headers = request.headers
                           .filterNot( _.isInstanceOf[Cookie] )
      val requestHeader = headers.find( _.name.toLowerCase == withHeaderName.toLowerCase )
                                 .map( _.value )

      requestHeader match {
        case None if headers.isEmpty => MatchResult(matches = false, "Request did not contain any headers.", "not-ok")
        case None => MatchResult(matches = false, s"Request contain header names: [${headers.map( _.name ).mkString(", ")}] which did not contain: [$withHeaderName]", "not-ok")
        case Some(value) if must.apply(value).matches => MatchResult(matches = true, "ok", "not-ok")
        case Some(value) => MatchResult(matches = false, s"Request header [$withHeaderName], did not match { ${must.apply(value).failureMessage} }", "not-ok")
      }
    }
  }

  private case class HeaderComparisonResult(identical: Seq[(String, String)], missing: Seq[(String, String)], extra: Seq[(String, String)])

}

trait RequestCookiesMatchers {
  def receivedCookieWith(name: String): RequestMatcher = receivedCookieThat(must = be(name) compose { (_: HttpCookiePair).name })

  def receivedCookieThat(must: Matcher[HttpCookiePair]): RequestMatcher = new RequestMatcher {

    def apply(request: HttpRequest): MatchResult = {
      val cookies = request.headers
                           .collect { case Cookie(cookie) => cookie }
                           .flatten[HttpCookiePair]
      val matchResult = cookies.map( c => must.apply(c) )
      if (matchResult.exists( _.matches) ) MatchResult(matches = true, "ok", "not-ok")
      else if (matchResult.isEmpty) MatchResult(matches = false, "Request did not contain any Cookie headers.", "Request did not contain any Cookie headers.")
      else
        MatchResult(matches = false,
          s"""Could not find cookie that matches for request contained cookies with names: [${cookies.map( c => s"'${c.name}'" ).mkString(", ")}]
             |${matchResult.map( _.failureMessage ).mkString("\n")}
           """.stripMargin,
          s"Request contained a cookie that matched, request has the following cookies: [${cookies.map( c => s"'${c.name}'" ).mkString(", ")}")
    }
  }
}

trait RequestBodyMatchers {
  import com.wix.e2e.http.WixHttpTestkitResources.{executionContext, materializer}

  def haveBodyWith(bodyContent: String): RequestMatcher = haveBodyThat( must = be(bodyContent) )
  def haveBodyThat(must: Matcher[String]): RequestMatcher = must compose httpRequestAsString

  def haveBodyWith(data: Array[Byte]): RequestMatcher = haveBodyDataThat( must = be(data) )
  def haveBodyDataThat(must: Matcher[Array[Byte]]): RequestMatcher = must compose httpRequestAsBinary

  def haveBodyWith[T <: Matcher[_]](entity: T): RequestMatcher = new RequestMatcher {
    def apply(left: HttpRequest): MatchResult =
      MatchResult(matches = false, "Matcher misuse: `haveBodyWith` received a matcher to match against, please use `haveBodyThat` instead.",
                                   "Matcher misuse: `haveBodyWith` received a matcher to match against, please use `haveBodyThat` instead.")
  }
  def haveBodyWith[T <: AnyRef : Manifest](entity: T)(implicit marshaller: Marshaller): RequestMatcher = haveBodyEntityThat[T]( must = be(entity) )
  def haveBodyEntityThat[T <: AnyRef : Manifest](must: Matcher[T])(implicit marshaller: Marshaller): RequestMatcher = new RequestMatcher {
    def apply(request: HttpRequest): MatchResult = {
      val content = waitFor( Unmarshal(request.entity).to[String] )
      handling(classOf[MissingMarshallerException], classOf[Exception])
        .by( {
          case e: MissingMarshallerException => throw e
          case e: Exception => throw new MarshallerErrorException(content, e)
        }) {
          val x = marshaller.unmarshall[T](content)
          val result = must(x)
          if (result.matches) MatchResult(matches = true, "ok", s"Failed to match: ['$x'] was equal to content: ['$content']")
          else MatchResult(matches = false, s"Failed to match: ['${result.failureMessageArgs.head}' != '${result.failureMessageArgs.last}'] with content: ['$content']",
                                            s"Failed to match: ['${result.failureMessageArgs.head}'] was not equal to ['${result.failureMessageArgs.last}'] for content: ['$content']")
        }
    }
  }

  private def httpRequestAsString = (r: HttpRequest) => waitFor( Unmarshal(r.entity).to[String] )
  private def httpRequestAsBinary = (r: HttpRequest) => waitFor( Unmarshal(r.entity).to[Array[Byte]] )
}

trait RequestRecorderMatchers {

  def receivedAnyOf[T <: RequestRecordSupport](requests: HttpRequest*): Matcher[T] =
    receivedRequestsInternal(requests, _.identical.nonEmpty,
                             res =>
                               s"""Could not find requests:
                                  |${requestsToStr(res.missing)}
                                  |
                                  |but found those:
                                  |${requestsToStr(res.extra)}""".stripMargin,
                             res =>
                               s"""Could find requests:
                                  |${requestsToStr(res.extra)}
                                  |
                                  |but didn't find those:
                                  |${requestsToStr(res.missing)}""".stripMargin )

  private def requestsToStr(rs: Seq[HttpRequest]) = rs.zipWithIndex.map{ case (r, i) => s"${i + 1}: $r"}.mkString(",\n")

  def receivedAllOf[T <: RequestRecordSupport](requests: HttpRequest*): Matcher[T] =
    receivedRequestsInternal( requests, _.missing.isEmpty,
                              res => s"""Could not find requests:
                                         |${requestsToStr(res.missing)}
                                         |
                                         |but found those:
                                         |${requestsToStr(res.identical)}""".stripMargin,
                              res => s"""Could find requests:
                                         |${requestsToStr(res.identical)}
                                         |
                                         |but didn't find those:
                                         |${requestsToStr(res.missing)}""".stripMargin)

  def receivedTheSameRequestsAs[T <: RequestRecordSupport](requests: HttpRequest*): Matcher[T] =
  receivedRequestsInternal( requests, r => r.extra.isEmpty && r.missing.isEmpty,
                            res => s"""Requests are not identical, missing requests are:
                                       |${requestsToStr(res.missing)}
                                       |
                                       |added requests found:
                                       |${requestsToStr(res.extra)}""".stripMargin,
                            res => s"""Requests are identical, requests found:
                                       |${requestsToStr(requests)}""".stripMargin)

  private def receivedRequestsInternal[T <: RequestRecordSupport](requests: Seq[HttpRequest], comparator: RequestComparisonResult => Boolean, errorMessage: RequestComparisonResult => String, negateErrorMessage: RequestComparisonResult => String): Matcher[T] = new Matcher[T] {
    def apply(recorder: T): MatchResult = {
      val recordedRequests = recorder.recordedRequests
      val comparisonResult = compare(requests, recordedRequests)

      if ( comparator(comparisonResult) ) MatchResult(matches = true, "ok", "ok")
      else if (recordedRequests.isEmpty) MatchResult(matches = false, "Server did not receive any requests.", "Server did not receive any requests.")
      else MatchResult(matches = false, errorMessage(comparisonResult), negateErrorMessage(comparisonResult))
    }

    private def compareRequest(request1: HttpRequest, request2: HttpRequest) = request1 == request2

    private def compare(headers: Seq[HttpRequest], requestHeaders: Seq[HttpRequest]): RequestComparisonResult = {
      val identical = headers.filter( h1 => requestHeaders.exists( h2 => compareRequest(h1, h2) ) )
      val missing = headers.filter( h1 => !identical.exists( h2 => compareRequest(h1, h2) ) )
      val extra = requestHeaders.filter( h1 => !identical.exists( h2 => compareRequest(h1, h2) ) )

      RequestComparisonResult(identical, missing, extra)
    }
  }

  def receivedAnyRequestThat[T <: RequestRecordSupport](must: Matcher[HttpRequest]): Matcher[T] = new Matcher[T] {

    def apply(request: T): MatchResult = {
      val recordedRequests = request.recordedRequests
      val results = recordedRequests.map( r => must.apply(r) )

      results match {
        case Nil => MatchResult(matches = false, "Server did not receive any requests.", "Server did not receive any requests.")
        case rs if rs.exists( _.matches ) => MatchResult(matches = true, "ok", "ok")
        case rs => MatchResult(matches = false, s"""Could not find any request that matches:
                                                   |${rs.zipWithIndex.map { case (r, i) => s"${i + 1}: ${r.failureMessage.replaceAll("\n", "")}" }.mkString(",\n") }""".stripMargin, "not-ok")
      }
    }
  }

  private case class RequestComparisonResult(identical: Seq[HttpRequest], missing: Seq[HttpRequest], extra: Seq[HttpRequest])
}

trait RequestContentTypeMatchers {
  def haveJsonBody: RequestMatcher = haveContent(ContentTypes.`application/json`)
  def haveTextPlainBody: RequestMatcher = haveContent(ContentTypes.`text/plain(UTF-8)`)
  def haveFormUrlEncodedBody: RequestMatcher = haveContent(MediaTypes.`application/x-www-form-urlencoded`.withCharset(HttpCharsets.`UTF-8`))
  def haveMultipartFormBody: RequestMatcher = haveMediaType(MediaTypes.`multipart/form-data`)


  private def haveContent(contentType: ContentType): RequestMatcher =
    be(contentType) compose { (_: HttpRequest).entity.contentType }
  private def haveMediaType(mediaType: MediaType): RequestMatcher =
    be(mediaType) compose { (_: HttpRequest).entity.contentType.mediaType.withParams(Map.empty) }
}
