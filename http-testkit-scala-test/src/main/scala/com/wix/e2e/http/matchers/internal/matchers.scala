package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.MediaType.NotCompressible
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.exceptions.{MarshallerErrorException, MissingMarshallerException}
import com.wix.e2e.http.matchers.ResponseMatcher
import com.wix.e2e.http.utils._
import com.wix.e2e.http.{HttpResponse, WixHttpTestkitResources}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.matchers.{MatchResult, Matcher}

import scala.util.control.Exception.handling
import scala.util.{Failure, Try}

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


  private def haveStatus(status: StatusCode): ResponseMatcher = be(status) compose httpResponseStatus
  private def httpResponseStatus = (_: HttpResponse).status
}


trait ResponseCookiesMatchers {
  def receivedCookieWith(name: String): ResponseMatcher = receivedCookieThat(must = be(name) compose { (_: HttpCookie).name })
  def receivedCookieThat(must: Matcher[HttpCookie]): ResponseMatcher = new ResponseMatcher {
    def apply(response: HttpResponse): MatchResult = {
      val cookies = response.headers
                            .collect { case `Set-Cookie`(cookie) => cookie }

      val matchResult = cookies.map( c => must.apply(c) )
      if (matchResult.exists( _.matches) ) MatchResult(matches = true, "ok", "not-ok")
      else if (matchResult.isEmpty) MatchResult(matches = false, "Response did not contain any `Set-Cookie` headers.", "not-ok")
      else MatchResult(matches = false, s"Could not find cookie that [${matchResult.map( _.failureMessage ).mkString(", ")}].", "not-ok")
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
    def apply(request: HttpResponse): MatchResult =  {
      val responseHeaders = request.headers
                                   .filterNot( _.isInstanceOf[`Set-Cookie`] )
                                   .map( h => h.name -> h.value )
      val comparisonResult = compare(headers, responseHeaders)

      if (matchAgainstContentTypeHeader)
        MatchResult(matches = false, """`Content-Type` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
                  |Use `haveContentType` matcher instead (or `beJsonResponse`, `beTextPlainResponse`, `beFormUrlEncodedResponse`).""".stripMargin, "not-ok")
      else if (matchAgainstContentLengthHeader)
        MatchResult(matches = false, """`Content-Length` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
                  |Use `haveContentLength` matcher instead.""".stripMargin, "not-ok")
      else if (matchAgainstTransferEncodingHeader)
        MatchResult(matches = false, """`Transfer-Encoding` is a special header and cannot be used in `haveAnyHeadersOf`, `haveAllHeadersOf`, `haveTheSameHeadersAs` matchers.
                  |Use `beChunkedResponse` or `haveTransferEncodings` matcher instead.""".stripMargin, "not-ok")
      else if ( comparator(comparisonResult) ) MatchResult(matches = true, "ok", "not-ok")
      else if (responseHeaders.isEmpty) MatchResult(matches = false, "Response did not contain any headers.", "not-ok")
      else MatchResult(matches = false, errorMessage(comparisonResult), "not-ok")
    }

    private def compareHeader(header1: (String, String), header2: (String, String)) = header1._1.toLowerCase == header2._1.toLowerCase && header1._2 == header2._2

    private def compare(headers: Seq[(String, String)], requestHeaders: Seq[(String, String)]): HeaderComparisonResult = {
      val identical = headers.filter( h1 => requestHeaders.exists( h2 => compareHeader(h1, h2) ) )
      val missing = headers.filter( h1 => !identical.exists( h2 => compareHeader(h1, h2) ) )
      val extra = requestHeaders.filter( h1 => !identical.exists( h2 => compareHeader(h1, h2) ) )

      HeaderComparisonResult(identical, missing, extra)
    }

    private def matchAgainstTransferEncodingHeader = headers.exists( h => "transfer-encoding".compareToIgnoreCase(h._1) == 0 )
    private def matchAgainstContentTypeHeader = headers.exists( h => "content-type".compareToIgnoreCase(h._1) == 0 )
    private def matchAgainstContentLengthHeader = headers.exists( h => "content-length".compareToIgnoreCase(h._1) == 0 )
  }

  def haveAnyHeaderThat(must: Matcher[String], withHeaderName: String): ResponseMatcher = new ResponseMatcher {

    def apply(request: HttpResponse): MatchResult = {
      val headers = request.headers
                           .filterNot( _.isInstanceOf[`Set-Cookie`] )
      val responseHeader = headers.find( _.name.toLowerCase == withHeaderName.toLowerCase )
                                  .map( _.value )

      responseHeader match {
        case None if headers.isEmpty => MatchResult(matches = false, "Response did not contain any headers.", "not-ok")
        case None => MatchResult(matches = false, s"Response contain header names: [${headers.map( _.name ).mkString(", ")}] which did not contain: [$withHeaderName]", "not-ok")
        case Some(v) if must.apply(v).matches => MatchResult(matches = true, "ok", "not-ok")
        case Some(v) => MatchResult(matches = false, s"Response header [$withHeaderName], did not match { ${must.apply(v).failureMessage} }", "not-ok")
      }
    }
  }

  private case class HeaderComparisonResult(identical: Seq[(String, String)], missing: Seq[(String, String)], extra: Seq[(String, String)])
}

trait ResponseBodyMatchers {
  import WixHttpTestkitResources.{executionContext, materializer}

  def haveBodyWith(bodyContent: String): ResponseMatcher = haveBodyThat( must = be(bodyContent) )
  def haveBodyThat(must: Matcher[String]): ResponseMatcher = must compose httpResponseAsString

  def haveBodyWith(data: Array[Byte]): ResponseMatcher = haveBodyDataThat( must = be(data) )
  def haveBodyDataThat(must: Matcher[Array[Byte]]): ResponseMatcher = must compose httpResponseAsBinary

  def haveBodyWith[T <: Matcher[_]](entity: T): ResponseMatcher = new ResponseMatcher {
    def apply(left: HttpResponse): MatchResult = MatchResult(matches = false, "Matcher misuse: `haveBodyWith` received a matcher to match against, please use `haveBodyThat` instead.", "not-ok")
  }
  def haveBodyWith[T <: AnyRef : Manifest](entity: T)(implicit marshaller: Marshaller): ResponseMatcher = haveBodyEntityThat[T]( must = be(entity) )
  def haveBodyEntityThat[T <: AnyRef : Manifest](must: Matcher[T])(implicit marshaller: Marshaller): ResponseMatcher = new ResponseMatcher {
    def apply(response: HttpResponse): MatchResult = {
      val content = waitFor( Unmarshal(response.entity).to[String] )

      handling(classOf[MissingMarshallerException], classOf[Exception])
        .by( {
          case e: MissingMarshallerException => throw e
          case e: Exception => throw new MarshallerErrorException(content, e)
        }) {
          val x = marshaller.unmarshall[T](content)
          val result = must.apply(x)
          if (result.matches) MatchResult(matches = true, "ok", "not-ok")
          else MatchResult(matches = false, s"Failed to match: ['${result.failureMessageArgs.head}' != '${result.failureMessageArgs.last}'] with content: [$content]", "not-ok")
        }
    }
  }

  private def httpResponseAsString = (r: HttpResponse) => waitFor( Unmarshal(r.entity).to[String] )
  private def httpResponseAsBinary = (r: HttpResponse) => waitFor( Unmarshal(r.entity).to[Array[Byte]] )
}

trait ResponseBodyAndStatusMatchers { self: ResponseBodyMatchers with ResponseStatusMatchers with ResponseHeadersMatchers with ResponseCookiesMatchers =>

  def beSuccessfulWith(bodyContent: String): ResponseMatcher = beSuccessful and haveBodyWith(bodyContent)
  def beSuccessfulWithBodyThat(must: Matcher[String]): ResponseMatcher = beSuccessful and haveBodyThat(must)

  def beSuccessfulWith[T <: Matcher[_]](entity: T): ResponseMatcher = new ResponseMatcher {
    def apply(left: HttpResponse): MatchResult = MatchResult(matches = false, "Matcher misuse: `beSuccessfulWith` received a matcher to match against, please use `beSuccessfulWithEntityThat` instead.", "not-ok")
  }
  def beSuccessfulWith[T <: AnyRef : Manifest](entity: T)(implicit marshaller: Marshaller): ResponseMatcher = beSuccessful and haveBodyWith(entity)
  def beSuccessfulWithEntityThat[T <: AnyRef : Manifest](must: Matcher[T])(implicit marshaller: Marshaller): ResponseMatcher = beSuccessful and haveBodyEntityThat[T](must)

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

  def beRedirectedTo(url: String): ResponseMatcher = haveLocationHeaderWith(url, beRedirect)
  def bePermanentlyRedirectedTo(url: String): ResponseMatcher = haveLocationHeaderWith(url, bePermanentlyRedirect)

  private def haveLocationHeaderWith(url: String, statusMatcher: ResponseMatcher): ResponseMatcher = new ResponseMatcher {

    def apply(response: HttpResponse): MatchResult = {
      val statusResult = statusMatcher.apply(response)
      if (!statusResult.matches) statusResult
      else {
        val header = response.header[`Location`]
        val expected = Try(Uri(url))
        (header, expected) match {
          case (_, Failure(_)) => MatchResult(matches = false, s"Matching against a malformed url: [$url].", "not-ok")
          case (None, _) => MatchResult(matches = false, "Response does not contain Location header.", "not-ok")
          case (Some(`Location`(a)), scala.util.Success(e)) if compareUrl(a, e) => MatchResult(matches = true, "ok", "not-ok")
          case (Some(`Location`(a)), _) =>
            MatchResult(matches = false, s"""Response is redirected to a different url:
                       |actual:   $a
                       |expected: $url
                       |""".stripMargin, "not-ok")
        }
      }
    }

    private def compareUrl(actual: Uri, expected: Uri) =
      actual.scheme == expected.scheme &&
      actual.authority == expected.authority &&
      actual.path == expected.path &&
      actual.query().toMap == expected.query().toMap &&
      actual.fragment == expected.fragment
  }
}

trait ResponseContentTypeMatchers {

  def beJsonResponse: ResponseMatcher = haveContentType(ContentTypes.`application/json`.value)
  def beTextPlainResponse: ResponseMatcher = haveContentType(MediaTypes.`text/plain`.value)
  def beFormUrlEncodedResponse: ResponseMatcher = haveContentType(MediaTypes.`application/x-www-form-urlencoded`.value)

  def haveContentType(contentType: String): ResponseMatcher = new ResponseMatcher {
    private val NoContentType = ContentType(MediaType.customBinary("none", "none", comp = NotCompressible))

    def apply(response: HttpResponse): MatchResult = {
      val actual = response.entity.contentType
      val expected = ContentType.parse(contentType)

      (actual, expected) match {
        case (a, _) if a == NoContentType => MatchResult(matches = false, "Request body does not have a set content type", "not-ok")
        case (_, Left(_)) => MatchResult(matches = false, s"Cannot match against a malformed content type: $contentType", "not-ok")
        case (a, Right(e)) if a == e => MatchResult(matches = true, "ok", "not-ok")
        case (a, Right(e)) if a != e => MatchResult(matches = false, s"Expected content type [$e] does not match actual content type [$a]", "not-ok")
      }
    }
  }
}

trait ResponseContentLengthMatchers {
  def haveContentLength(length: Long): ResponseMatcher = haveContentWithOptionalLengthOf(Some(length))
  def haveNoContentLength: ResponseMatcher = haveContentWithOptionalLengthOf(None)

  private def haveContentWithOptionalLengthOf(expected: Option[Long]): ResponseMatcher = new ResponseMatcher {
    def apply(response: HttpResponse): MatchResult = {
      val actual = response.entity.contentLengthOption

      (actual, expected) match {
        case (Some(a), Some(e)) if a == e => MatchResult(matches = true, "ok", "not-ok")
        case (Some(a), Some(e)) if a != e => MatchResult(matches = false, s"Expected content length [$e] does not match actual content length [$a]", "not-ok")
        case (None, Some(e)) => MatchResult(matches = false, s"Expected content length [$e] but response did not contain `content-length` header.", "not-ok")
        case (Some(a), None) => MatchResult(matches = false, s"Expected no `content-length` header but response did contain `content-length` header with size [$a].", "not-ok")
        case (None, None) => MatchResult(matches = true, "ok", "not-ok")
      }
    }
  }
}

trait ResponseTransferEncodingMatchers {

  def beChunkedResponse: ResponseMatcher = new ResponseMatcher {
    def apply(response: HttpResponse): MatchResult = {
      val encodings = response.header[`Transfer-Encoding`]
                              .map( _.encodings.map( _.name ) )

      if (response.entity.isChunked || encodings.exists( _.contains("chunked") )) MatchResult(matches = true, "ok", "not-ok")
      else if (encodings.isEmpty) MatchResult(matches = false, "Expected Chunked response while response did not contain `Transfer-Encoding` header", "not-ok")
      else MatchResult(matches = false, s"Expected Chunked response while response has `Transfer-Encoding` header with values [${encodings.toSeq.flatten.map(s => s"'$s'").mkString(", ")}]", "not-ok")
    }
  }

  def haveTransferEncodings(encodings: String*): ResponseMatcher = new ResponseMatcher {
    def apply(response: HttpResponse): MatchResult = {
      val actual = response.header[`Transfer-Encoding`]
                           .map( _.encodings.map( _.name ).toSet )
                           .getOrElse( Set.empty )
      val expected = encodings.toSet

      (actual, expected) match {
        case (a, e) if a.isEmpty && e.nonEmpty => MatchResult(matches = false, "Response did not contain `Transfer-Encoding` header.", "not-ok")
        case (a, e) if e.forall( a.contains ) => MatchResult(matches = true, "ok", "not-ok")
        case (a, e) if e.exists( x => !a.contains(x) ) => MatchResult(matches = false, s"Expected transfer encodings [${e.map(s => s"'$s'").mkString(", ")}] does not match actual transfer encoding [${a.toSeq.sorted.map(s => s"'$s'").mkString(", ")}]", "not-ok")
      }
    }
  }
}

trait ResponseSpecialHeadersMatchers extends ResponseContentTypeMatchers
                                        with ResponseContentLengthMatchers
                                        with ResponseTransferEncodingMatchers
