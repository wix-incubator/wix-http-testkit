package com.wix.hoopoe.http.matchers.internal

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.headers._
import com.wix.hoopoe.http.HttpResponse
import com.wix.hoopoe.http.matchers.ResponseMatcher
import org.specs2.matcher.Matchers._
import akka.http.scaladsl.model.StatusCodes._
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


