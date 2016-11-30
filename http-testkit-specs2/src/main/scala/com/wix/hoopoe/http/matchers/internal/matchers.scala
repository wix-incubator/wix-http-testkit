package com.wix.hoopoe.http.matchers.internal

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpResponse, StatusCode}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.wix.hoopoe.http.WixHttpTestkitResources
import com.wix.hoopoe.http.exceptions.ConnectionRefusedException
import com.wix.hoopoe.http.matchers.ResponseMatcher
import com.wix.hoopoe.http.matchers.json.{DefaultMarshaller, Marshaller}
import com.wix.hoopoe.http.utils._
import org.specs2.matcher.Matchers._
import org.specs2.matcher.{Expectable, MatchResult, Matcher}

import scala.concurrent.ExecutionContext
import scala.util.Try

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


  // client errors
  def beConnectionRefused: ResponseMatcher = throwA[ConnectionRefusedException]


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

trait ResponseBodyMatchers {
  import WixHttpTestkitResources.materializer

  import ExecutionContext.Implicits.global


  def haveBodyWith(bodyContent: String): ResponseMatcher = haveBodyThat( must = be_===(bodyContent) )
  def haveBodyThat(must: Matcher[String]): ResponseMatcher = must ^^ httpResponseAsString

  def haveBodyWith(data: Array[Byte]): ResponseMatcher = haveBodyDataThat( must = be_===(data) )
  def haveBodyDataThat(must: Matcher[Array[Byte]]): ResponseMatcher = must ^^ httpResponseAsBinary

  def havePayloadWith[T <: AnyRef : Manifest](entity: T)(implicit marshaller: Marshaller = DefaultMarshaller.marshaller): ResponseMatcher = havePayloadThat[T]( must = be_===(entity) )
  def havePayloadThat[T <: AnyRef : Manifest](must: Matcher[T])(implicit marshaller: Marshaller = DefaultMarshaller.marshaller): ResponseMatcher = new ResponseMatcher {

    def apply[S <: HttpResponse](t: Expectable[S]): MatchResult[S] = {
      val response = t.value
      val content = waitFor( Unmarshal(response.entity).to[String] )

      Try( marshaller.unmarshall[T](content) ).toOption match {
        case None => failure(s"Failed to unmarshall: [$content]", t)
        case Some(x) if must.apply(createExpectable(x)).isSuccess => success("ok", t)
        case Some(x) => failure(s"Failed to match: [${must.apply(createExpectable(x)).message.replaceAll("\n", "")}] with content: [$content]", t)
      }
    }
  }

  private def httpResponseAsString = (r: HttpResponse) => waitFor( Unmarshal(r.entity).to[String] ) aka "Body content as string"
  private def httpResponseAsBinary = (r: HttpResponse) => waitFor( Unmarshal(r.entity).to[Array[Byte]] ) aka "Body content as bytes"
}

trait ResponseBodyAndStatusMatchers { self: ResponseBodyMatchers with ResponseStatusMatchers with ResponseHeadersMatchers with ResponseCookiesMatchers =>

  def beSuccessfulWith(bodyContent: String): ResponseMatcher = beSuccessful and haveBodyWith(bodyContent)
  def beSuccessfulWithBodyThat(must: Matcher[String]): ResponseMatcher = beSuccessful and haveBodyThat(must)

  def beSuccessfulWith[T <: AnyRef : Manifest](entity: T)(implicit marshaller: Marshaller = DefaultMarshaller.marshaller): ResponseMatcher = beSuccessful and havePayloadWith(entity)
  def beSuccessfulWithEntityThat[T <: AnyRef : Manifest](must: Matcher[T])(implicit marshaller: Marshaller = DefaultMarshaller.marshaller): ResponseMatcher = beSuccessful and havePayloadThat(must)

  def beSuccessfulWith(data: Array[Byte]): ResponseMatcher = beSuccessful and haveBodyWith(data)
  def beSuccessfulWithBodyDataThat(must: Matcher[Array[Byte]]): ResponseMatcher = beSuccessful and haveBodyDataThat(must)

  def beSuccessfulWithHeaders(headers: (String, String)*): ResponseMatcher = beSuccessful and haveAllOf(headers:_*)
  def beSuccessfulWithHeaderThat(must: Matcher[String], withHeaderName: String): ResponseMatcher = beSuccessful and haveAnyHeaderThat(must, withHeaderName)

  def beSuccessfulWithCookie(cookieName: String): ResponseMatcher = beSuccessful and receivedCookieWith(cookieName)
  def beSuccessfulWithCookieThat(must: Matcher[HttpCookie]): ResponseMatcher = beSuccessful and receivedCookieThat(must)

  def beInvalidWith(bodyContent: String): ResponseMatcher = beInvalid and haveBodyWith(bodyContent)
  def beInvalidWithBodyThat(must: Matcher[String]): ResponseMatcher = beInvalid and haveBodyThat(must)
}
