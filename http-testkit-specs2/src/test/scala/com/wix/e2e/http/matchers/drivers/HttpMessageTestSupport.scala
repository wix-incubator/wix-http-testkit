package com.wix.e2e.http.matchers.drivers

import akka.http.scaladsl.model.ContentTypes.`text/plain(UTF-8)`
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.Uri.{Path, Query}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.scaladsl.Source
import com.wix.e2e.http.matchers.drivers.MarshallingTestObjects.SomeCaseClass
import com.wix.test.random._
import org.specs2.matcher.CaseClassDiffs._
import org.specs2.matcher.Matcher
import org.specs2.matcher.Matchers._

import scala.collection.immutable
import scala.util.Random

trait HttpMessageTestSupport {

  val cookie = randomCookie
  val anotherCookie = randomCookie
  val yetAnotherCookie = randomCookie

  val cookiePair = randomStrPair
  val anotherCookiePair = randomStrPair
  val yetAnotherCookiePair = randomStrPair

  val nonExistingHeaderName = randomStr
  val header = randomHeader
  val anotherHeader = randomHeader
  val yetAnotherHeader = randomHeader
  val andAnotherHeader = randomHeader

  val somePath = randomPath
  val anotherPath = randomPath

  val parameter = randomParameter
  val anotherParameter = randomParameter
  val yetAnotherParameter = randomParameter
  val andAnotherParameter = randomParameter

  val nonExistingParamName = randomStr

  val content = randomStr
  val anotherContent = randomStr

  val length = randomInt(1, 30)
  val anotherLength = randomInt(32, 60)

  def contentWith(length: Int) = randomStrWith(length = length)

  val binaryContent = Array[Byte](1, 1, 1, 1)
  val anotherBinaryContent = Array[Byte](2, 2, 2, 2)

  val malformedContentType = randomStr
  val contentType = "application/json"
  val anotherContentType = "text/plain"
  val contentTypeHeader = "content-type" -> contentType
  val transferEncodingHeader = "transfer-encoding" -> randomStr
  val contentLengthHeader = "content-length" -> randomInt(1, 66666).toString

  val someObject = SomeCaseClass(randomStr, randomInt)
  val anotherObject = SomeCaseClass(randomStr, randomInt)

  val url = "http://example.com/some/path"
  val malformedUrl = "http://www.example.com/name with spaces/"
  val anotherUrl = "http://example.com/another/path"

  val `application/x-www-form-urlencoded` = MediaTypes.`application/x-www-form-urlencoded`
  val `multipart/form-data` = MediaTypes.`multipart/form-data`

  def randomStatusThatIsNot(status: StatusCode): StatusCode =
    Random.shuffle(AllResponseStatuses.filterNot(_ == status))
          .head

  def randomStatus: StatusCode =
    Random.shuffle(AllResponseStatuses)
          .head

  def randomMethodThatIsNot(method: HttpMethod) =
    Random.shuffle(Seq(CONNECT, DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT, TRACE).filterNot( _ == method)).head


  private val AllResponseStatuses =
    Seq(Continue, SwitchingProtocols, Processing, OK, Created, Accepted, NonAuthoritativeInformation,
        NoContent, ResetContent, PartialContent, MultiStatus, AlreadyReported, IMUsed, MultipleChoices,
        MovedPermanently, Found, SeeOther, NotModified, UseProxy, TemporaryRedirect, PermanentRedirect,
        BadRequest, Unauthorized, PaymentRequired, Forbidden, NotFound, MethodNotAllowed, NotAcceptable,
        ProxyAuthenticationRequired, RequestTimeout, Conflict, Gone, LengthRequired, PreconditionFailed,
        RequestEntityTooLarge, RequestUriTooLong, UnsupportedMediaType, RequestedRangeNotSatisfiable,
        ExpectationFailed, EnhanceYourCalm, UnprocessableEntity, Locked, FailedDependency,
        UpgradeRequired, PreconditionRequired, TooManyRequests, RequestHeaderFieldsTooLarge, RetryWith,
        BlockedByParentalControls, UnavailableForLegalReasons, InternalServerError, NotImplemented, BadGateway,
        ServiceUnavailable, GatewayTimeout, HTTPVersionNotSupported, VariantAlsoNegotiates, InsufficientStorage,
        LoopDetected, BandwidthLimitExceeded, NotExtended, NetworkAuthenticationRequired, NetworkReadTimeout,
        NetworkConnectTimeout)


  private def randomCookie = HttpCookie(randomStr, randomStr)
}

object HttpResponseFactory {

  def aResponseWithNoCookies = aResponseWithCookies()
  def aResponseWithCookies(cookies: HttpCookie*) =
    HttpResponse(headers = immutable.Seq( cookies.map( `Set-Cookie`(_) ):_* ) )

  def aResponseWithNoHeaders = aResponseWithHeaders()
  def aResponseWithHeaders(headers: (String, String)*) = HttpResponse(headers = immutable.Seq( headers.map{ case (k, v) => RawHeader(k, v) }:_* ) )

  def aSuccessfulResponseWith(body: String) = aResponseWith(body).copy(status = OK)
  def aSuccessfulResponseWith(binaryBody: Array[Byte]) = aResponseWith(binaryBody).copy(status = OK)
  def aSuccessfulResponseWith(headers: (String, String)*) = aResponseWithHeaders(headers:_*).copy(status = OK)
  def aSuccessfulResponseWithCookies(cookies: HttpCookie*) = aResponseWithCookies(cookies:_*).copy(status = OK)

  def aResponseWith(status: StatusCode) = HttpResponse(status)

  def aRedirectResponseTo(url: String) = HttpResponse(status = Found, headers = immutable.Seq( Location(Uri(url)) ) )
  def aRedirectResponseWithoutLocationHeader = aRedirectResponseTo("http://example.com").removeHeader("Location")
  def aPermanentlyRedirectResponseTo(url: String) = aRedirectResponseTo(url).copy(status = MovedPermanently)
  def aPermanentlyRedirectResponseWithoutLocationHeader = aRedirectResponseWithoutLocationHeader.copy(status = MovedPermanently)

  def aResponseWith(body: String) = HttpResponse(entity = body)
  def aResponseWith(binaryBody: Array[Byte]) = HttpResponse(entity = binaryBody)
  def aResponseWithoutBody = HttpResponse()
  def aResponseWithContentType(contentType: String) = {
    val r = aResponseWithoutBody
    aResponseWithoutBody.copy(entity = r.entity.withContentType(ContentType.parse(contentType) match {
      case Right(c) => c
      case Left(_) => throw new IllegalArgumentException(contentType)
    }))
  }

  def aChunkedResponse = 
    HttpResponse(entity = HttpEntity.Chunked(ContentTypes.`text/plain(UTF-8)`, Source.single(randomStr)),
                 headers = immutable.Seq(`Transfer-Encoding`(TransferEncodings.chunked)))
  
  def aChunkedResponseWith(transferEncoding: TransferEncoding) =
    HttpResponse(entity = HttpEntity.Chunked(ContentTypes.`text/plain(UTF-8)`, Source.single(randomStr)),
                 headers = immutable.Seq(`Transfer-Encoding`(transferEncoding, TransferEncodings.chunked)) )

  def aResponseWithoutTransferEncoding = HttpResponse()
  def aResponseWithTransferEncodings(transferEncoding: TransferEncoding, tail: TransferEncoding*) =
    HttpResponse(headers = immutable.Seq(`Transfer-Encoding`(transferEncoding, tail:_*)))

  def aResponseWithoutContentLength =
    HttpResponse(entity = Multipart.FormData(Multipart.FormData
                                                      .BodyPart(randomStr,
                                                                HttpEntity.IndefiniteLength(`text/plain(UTF-8)`,
                                                                                            Source(immutable.Iterable.newBuilder.result))))
                                   .toEntity)

  def anInvalidResponseWith(body: String) = aResponseWith(body).copy(status = BadRequest)
}

object HttpResponseMatchers {
  def cookieWith(value: String): Matcher[HttpCookie] = be_===(value) ^^ { (_: HttpCookie).value aka "cookie value" }
}

object MarshallingTestObjects {
  case class SomeCaseClass(s: String, i: Int)
}


object HttpRequestFactory {

  def aRequestWith(method: HttpMethod) = HttpRequest(method = method)
  def aRequestWithPath(path: String) = HttpRequest(uri = Uri().withPath(Path(path)))

  def aRequestWithParameters(parameters: (String, String)*) = HttpRequest(uri = Uri().withQuery(Query(parameters:_*)))
  def aRequestWithNoParameters = HttpRequest()

  def aRequestWithHeaders(headers: (String, String)*) = HttpRequest(headers = immutable.Seq( headers.map{ case (k, v) => RawHeader(k, v) }:_* ) )
  def aRequestWithNoHeaders = aRequestWithHeaders()

  def aRequestWithNoCookies = HttpRequest()
  def aRequestWithCookies(cookies: (String, String)*) =
    HttpRequest(headers = immutable.Seq( cookies.map( Cookie(_) ) :_*) )

  def aRequestWith(body: String) = HttpRequest(entity = body)
  def aRequestWith(contentType: ContentType) = HttpRequest(entity = HttpEntity.Empty.withContentType(contentType))
  def aRequestWith(binaryBody: Array[Byte]) = HttpRequest(entity = binaryBody)
  def aRequestWithoutBody = HttpRequest()

  def aRandomRequest = aRequestWithPath(randomStr)
}
