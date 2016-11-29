package com.wix.hoopoe.http.matchers.internal

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers._
import com.wix.hoopoe.http.HttpResponse
import com.wix.hoopoe.http.matchers.ResponseMatcher
import org.specs2.matcher.Matchers._
import org.specs2.matcher.{Expectable, MatchResult, Matcher}

trait ResponseStatusMatchers {

  // Successful Response Code
  def beSuccessful: ResponseMatcher = haveStatus(OK)
  def beSuccessfullyCreated: ResponseMatcher = haveStatus(Created)
  def beAccepted: ResponseMatcher = haveStatus(Accepted)
  def beNoContent: ResponseMatcher = haveStatus(NoContent)


  // Redirect response codes
  def beRedirect: ResponseMatcher = haveStatus(Found)
  def bePermanentlyRedirect: ResponseMatcher = haveStatus(MovedPermanently)


  // Request Error Response Codes
  def beRejected: ResponseMatcher = haveStatus(Forbidden)
  def beNotFound: ResponseMatcher = haveStatus(NotFound)
  def beInvalid: ResponseMatcher = haveStatus(BadRequest)
  def beRejectedTooLarge: ResponseMatcher = haveStatus(RequestEntityTooLarge)
  def beUnauthorized: ResponseMatcher = haveStatus(Unauthorized)
  def beNotSupported: ResponseMatcher = haveStatus(MethodNotAllowed)
  def beConflict: ResponseMatcher = haveStatus(Conflict)
  def bePreconditionFailed: ResponseMatcher = haveStatus(PreconditionFailed)
  def beUnprocessableEntity: ResponseMatcher = haveStatus(UnprocessableEntity)
  def bePreconditionRequired: ResponseMatcher = haveStatus(PreconditionRequired)
  def beTooManyRequests: ResponseMatcher = haveStatus(TooManyRequests)


  // Server Error Response codes
  def beUnavailable: ResponseMatcher = haveStatus(ServiceUnavailable)
  def beInternalServerError: ResponseMatcher = haveStatus(InternalServerError)
  def beNotImplemented: ResponseMatcher = haveStatus(NotImplemented)


  private def haveStatus(status: StatusCode): ResponseMatcher = be_===(status) ^^ httpResponseStatus
  private def httpResponseStatus = (_: HttpResponse).status aka "response status"
}


trait ResponseCookiesMatchers {

  def receivedCookieWith(name: String): ResponseMatcher = receivedCookieThat(must = be_===(name) ^^ { (_: HttpCookie).name aka "cookie name" })

  def receivedCookieThat(must: Matcher[HttpCookie]): ResponseMatcher = new ResponseMatcher {
    def apply[S <: HttpResponse](t: Expectable[S]): MatchResult[S] = {
      val response = t.value
      val cookies = response.headers
                            .collect { case `Set-Cookie`(cookie) => cookie }

      val matchResult = cookies.map( c => must.apply(createExpectable(c)) )
      if (matchResult.exists( _.isSuccess) ) success("ok", t)
      else if (matchResult.isEmpty) failure("Response did not contain any `Set-Cookie` headers.", t)
      else failure(s"Could not find cookie that [${matchResult.map( _.message ).mkString(", ")}].", t)
    }
  }
}

trait ResponseHeadersMatchers {
  def haveAnyOf(headers: (String, String)*): ResponseMatcher =
    haveHeaderInternal( headers, _.identical.nonEmpty,
                        res => s"Could not find header [${res.missing.map(_._1).mkString(", ")}] but found those: [${res.extra.map(_._1).mkString(", ")}]" )

  def haveAllOf(headers: (String, String)*): ResponseMatcher =
    haveHeaderInternal( headers, _.missing.isEmpty,
                        res => s"Could not find header [${res.missing.map(_._1).mkString(", ")}] but found those: [${res.identical.map(_._1).mkString(", ")}]." )

  def haveTheSameHeadersAs(headers: (String, String)*): ResponseMatcher =
    haveHeaderInternal( headers, r => r.extra.isEmpty && r.missing.isEmpty,
                        res => s"Request header is not identical, missing headers from request: [${res.missing.map(_._1).mkString(", ")}], request contained extra headers: [${res.extra.map(_._1).mkString(", ")}]." )

  private def haveHeaderInternal(headers: Seq[(String, String)], comparator: HeaderComparisonResult => Boolean, errorMessage: HeaderComparisonResult => String): ResponseMatcher = new ResponseMatcher {

    def apply[S <: HttpResponse](t: Expectable[S]): MatchResult[S] = {
      val request = t.value
      val responseHeaders = request.headers
                                   .filterNot( _.isInstanceOf[`Set-Cookie`] )
                                   .map( h => h.name -> h.value )
      val comparisonResult = compare(headers, responseHeaders)

      if ( comparator(comparisonResult) ) success("ok", t)
      else if (responseHeaders.isEmpty) failure("Response did not contain any headers.", t)
      else failure(errorMessage(comparisonResult), t)
    }

    private def compareHeader(header1: (String, String), header2: (String, String)) = header1._1.toLowerCase == header2._1.toLowerCase && header1._2 == header2._2

    private def compare(headers: Seq[(String, String)], requestHeaders: Seq[(String, String)]): HeaderComparisonResult = {
      val identical = headers.filter( h1 => requestHeaders.exists( h2 => compareHeader(h1, h2) ) )
      val missing = headers.filter( h1 => !identical.exists( h2 => compareHeader(h1, h2) ) )
      val extra = requestHeaders.filter( h1 => !identical.exists( h2 => compareHeader(h1, h2) ) )

      HeaderComparisonResult(identical, missing, extra)
    }
  }

  def haveAnyHeaderThat(must: Matcher[String], withHeaderName: String): ResponseMatcher = new ResponseMatcher {
    def apply[S <: HttpResponse](t: Expectable[S]): MatchResult[S] = {
      val request = t.value
      val headers = request.headers
                           .filterNot( _.isInstanceOf[`Set-Cookie`] )
      val responseHeader = headers.find( _.name.toLowerCase == withHeaderName.toLowerCase )
                                  .map( _.value )

      responseHeader match {
        case None if headers.isEmpty => failure("Response did not contain any headers.", t)
        case None => failure(s"Response contain header names: [${headers.map( _.name ).mkString(", ")}] which did not contain: [$withHeaderName]", t)
        case Some(value) if must.apply(createExpectable(value)).isSuccess => success("ok", t)
        case Some(value) => failure(s"Response header [$withHeaderName], did not match { ${must.apply(createExpectable(value)).message} }", t)
      }
    }
  }

  private case class HeaderComparisonResult(identical: Seq[(String, String)], missing: Seq[(String, String)], extra: Seq[(String, String)])
}
