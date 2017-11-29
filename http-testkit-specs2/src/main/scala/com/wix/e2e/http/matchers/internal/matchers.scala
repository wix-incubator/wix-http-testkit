package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.MediaType.NotCompressible
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.exceptions.{ConnectionRefusedException, MarshallerErrorException, MissingMarshallerException}
import com.wix.e2e.http.matchers.ResponseMatcher
import com.wix.e2e.http.utils._
import com.wix.e2e.http.{HttpResponse, WixHttpTestkitResources}
import org.specs2.matcher.Matchers._
import org.specs2.matcher.{Expectable, MatchResult, Matcher}

import scala.util.control.Exception.handling

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


  def haveStatus(code: Int): ResponseMatcher = haveStatus( StatusCode.int2StatusCode(code) )

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
  def haveAnyHeadersOf(headers: (String, String)*): ResponseMatcher =
    haveHeaderInternal( headers, _.identical.nonEmpty,
                        res => s"Could not find header [${res.missing.map(_._1).mkString(", ")}] but found those: [${res.extra.map(_._1).mkString(", ")}]" )

  def haveAllHeadersOf(headers: (String, String)*): ResponseMatcher =
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

      if (matchAgainstContentTypeHeader)
        failure("""`content-type` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
                  |Use `haveContentType` matcher instead (or `beJsonResponse`, `beTextPlainResponse`, `beFormUrlEncodedResponse`).""".stripMargin, t)
      else if (matchAgainstContentLengthHeader)
        failure("""`content-length` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
                  |Use `haveContentLength` matcher instead.""".stripMargin, t)
      else if ( comparator(comparisonResult) ) success("ok", t)
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

    private def matchAgainstContentTypeHeader = headers.exists( h => "content-type".compareToIgnoreCase(h._1) == 0 )
    private def matchAgainstContentLengthHeader = headers.exists( h => "content-length".compareToIgnoreCase(h._1) == 0 )
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
  import WixHttpTestkitResources.{executionContext, materializer}

  def haveBodyWith(bodyContent: String): ResponseMatcher = haveBodyThat( must = be_===(bodyContent) )
  def haveBodyThat(must: Matcher[String]): ResponseMatcher = must ^^ httpResponseAsString

  def haveBodyWith(data: Array[Byte]): ResponseMatcher = haveBodyDataThat( must = be_===(data) )
  def haveBodyDataThat(must: Matcher[Array[Byte]]): ResponseMatcher = must ^^ httpResponseAsBinary

  def haveBodyWith[T <: AnyRef : Manifest](entity: T)(implicit marshaller: Marshaller): ResponseMatcher = haveBodyThat[T]( must = be_===(entity) )
  def haveBodyThat[T <: AnyRef : Manifest](must: Matcher[T])(implicit marshaller: Marshaller): ResponseMatcher = new ResponseMatcher {

    def apply[S <: HttpResponse](t: Expectable[S]): MatchResult[S] = {
      val response = t.value
      val content = waitFor( Unmarshal(response.entity).to[String] )

      handling(classOf[MissingMarshallerException], classOf[Exception])
        .by( {
          case e: MissingMarshallerException => throw e
          case e: Exception => throw new MarshallerErrorException(content, e)
        }) {
          val x = marshaller.unmarshall[T](content)
          if (must.apply(createExpectable(x)).isSuccess) success("ok", t)
          else failure(s"Failed to match: [${must.apply(createExpectable(x)).message.replaceAll("\n", "")}] with content: [$content]", t)
        }
    }
  }

  private def httpResponseAsString = (r: HttpResponse) => waitFor( Unmarshal(r.entity).to[String] ) aka "Body content as string"
  private def httpResponseAsBinary = (r: HttpResponse) => waitFor( Unmarshal(r.entity).to[Array[Byte]] ) aka "Body content as bytes"
}

trait ResponseBodyAndStatusMatchers { self: ResponseBodyMatchers with ResponseStatusMatchers with ResponseHeadersMatchers with ResponseCookiesMatchers =>

  def beSuccessfulWith(bodyContent: String): ResponseMatcher = beSuccessful and haveBodyWith(bodyContent)
  def beSuccessfulWithBodyThat(must: Matcher[String]): ResponseMatcher = beSuccessful and haveBodyThat(must)

  def beSuccessfulWith[T <: AnyRef : Manifest](entity: T)(implicit marshaller: Marshaller): ResponseMatcher = beSuccessful and haveBodyWith(entity)
  def beSuccessfulWithEntityThat[T <: AnyRef : Manifest](must: Matcher[T])(implicit marshaller: Marshaller): ResponseMatcher = beSuccessful and haveBodyThat(must)

  def beSuccessfulWith(data: Array[Byte]): ResponseMatcher = beSuccessful and haveBodyWith(data)
  def beSuccessfulWithBodyDataThat(must: Matcher[Array[Byte]]): ResponseMatcher = beSuccessful and haveBodyDataThat(must)

  def beSuccessfulWithHeaders(headers: (String, String)*): ResponseMatcher = beSuccessful and haveAllHeadersOf(headers:_*)
  def beSuccessfulWithHeaderThat(must: Matcher[String], withHeaderName: String): ResponseMatcher = beSuccessful and haveAnyHeaderThat(must, withHeaderName)

  def beSuccessfulWithCookie(cookieName: String): ResponseMatcher = beSuccessful and receivedCookieWith(cookieName)
  def beSuccessfulWithCookieThat(must: Matcher[HttpCookie]): ResponseMatcher = beSuccessful and receivedCookieThat(must)

  def beInvalidWith(bodyContent: String): ResponseMatcher = beInvalid and haveBodyWith(bodyContent)
  def beInvalidWithBodyThat(must: Matcher[String]): ResponseMatcher = beInvalid and haveBodyThat(must)
}

trait ResponseStatusAndHeaderMatchers { self: ResponseStatusMatchers with ResponseHeadersMatchers =>

  def beRedirectedTo(url: String): ResponseMatcher = beRedirect and haveLocationHeaderWith(url)
  def bePermanentlyRedirectedTo(url: String): ResponseMatcher = bePermanentlyRedirect and haveLocationHeaderWith(url)

  private def haveLocationHeaderWith(value: String) = haveAnyHeaderThat(be_===(value), withHeaderName = "Location")
}

trait ResponseContentTypeMatchers {

  def beJsonResponse: ResponseMatcher = haveContentType(ContentTypes.`application/json`.value)
  def beTextPlainResponse: ResponseMatcher = haveContentType(MediaTypes.`text/plain`.value)
  def beFormUrlEncodedResponse: ResponseMatcher = haveContentType(MediaTypes.`application/x-www-form-urlencoded`.value)

  def haveContentType(contentType: String): ResponseMatcher = new ResponseMatcher {
    private val NoContentType = ContentType(MediaType.customBinary("none", "none", comp = NotCompressible))

    def apply[S <: HttpResponse](t: Expectable[S]) = {
      val response = t.value
      val actual = response.entity.contentType
      val expected = ContentType.parse(contentType)

      (actual, expected) match {
        case (a, _) if a == NoContentType => failure("Request body does not have a set content type", t)
        case (_, Left(_)) => failure(s"Cannot match against a malformed content type: $contentType", t)
        case (a, Right(e)) if a == e => success("ok", t)
        case (a, Right(e)) if a != e => failure(s"Expected content type [$e] does not match actual content type [$a]", t)
      }
    }
  }
}

trait ResponseContentLengthMatchers {
  def haveContentLength(length: Long): ResponseMatcher = haveContentWithOptionalLengthOf(Some(length))
  def haveNoContentLength: ResponseMatcher = haveContentWithOptionalLengthOf(None)

  private def haveContentWithOptionalLengthOf(expected: Option[Long]): ResponseMatcher = new ResponseMatcher {
    def apply[S <: HttpResponse](t: Expectable[S]) = {
      val response = t.value
      val actual = response.entity.contentLengthOption

      (actual, expected) match {
        case (Some(a), Some(e)) if a == e => success("ok", t)
        case (Some(a), Some(e)) if a != e => failure(s"Expected content length [$e] does not match actual content length [$a]", t)
        case (None, Some(e)) => failure(s"Expected content length [$e] but response did not contain `content-length` header.", t)
        case (Some(a), None) => failure(s"Expected no `content-length` header but response did contain `content-length` header with size [$a].", t)
        case (None, None) => success("ok", t)
      }
    }
  }
}

trait ResponseSpecialHeadersMatchers extends ResponseContentTypeMatchers
                                        with ResponseContentLengthMatchers
