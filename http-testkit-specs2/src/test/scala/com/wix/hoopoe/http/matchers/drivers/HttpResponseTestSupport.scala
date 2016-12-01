package com.wix.hoopoe.http.matchers.drivers

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.Uri.{Path, Query}
import akka.http.scaladsl.model.headers.{Cookie, HttpCookie, RawHeader, `Set-Cookie`}
import akka.http.scaladsl.model._
import com.wix.hoopoe.http.matchers.drivers.MarshallingTestObjects.SomeCaseClass
import com.wixpress.hoopoe.test._
import org.specs2.matcher.Matcher
import org.specs2.matcher.Matchers._

import scala.collection.immutable
import scala.util.Random

trait HttpResponseTestSupport {

  val cookie = randomCookie
  val anotherCookie = randomCookie
  val yetAnotherCookie = randomCookie

  val cookiePair = randomStrPair

  val nonExistingHeaderName = randomStr
  val header = randomHeader
  val anotherHeader = randomHeader
  val yetAnotherHeader = randomHeader
  val andAnotherHeader = randomHeader

  val content = randomStr
  val anotherContent = randomStr

  val binaryContent = Array[Byte](1, 1, 1, 1)
  val anotherBinaryContent = Array[Byte](2, 2, 2, 2)

  val someObject = SomeCaseClass(randomStr, randomInt)
  val anotherObject = SomeCaseClass(randomStr, randomInt)

  def randomStatusThatIsNot(status: StatusCode): StatusCode =
    Random.shuffle(AllResponseStatuses.filterNot(_ == status))
          .head

  private val AllResponseStatuses =
    Seq(Continue, SwitchingProtocols, Processing, OK, Created, Accepted, NonAuthoritativeInformation,
        NoContent, ResetContent, PartialContent, MultiStatus, AlreadyReported, IMUsed, MultipleChoices,
        MovedPermanently, Found, SeeOther, NotModified, UseProxy, TemporaryRedirect, PermanentRedirect,
        BadRequest, Unauthorized, PaymentRequired, Forbidden, NotFound, MethodNotAllowed, NotAcceptable,
        ProxyAuthenticationRequired, RequestTimeout, Conflict, Gone, LengthRequired, PreconditionFailed,
        RequestEntityTooLarge, RequestUriTooLong, UnsupportedMediaType, RequestedRangeNotSatisfiable,
        ExpectationFailed, EnhanceYourCalm, UnprocessableEntity, Locked, FailedDependency, UnorderedCollection,
        UpgradeRequired, PreconditionRequired, TooManyRequests, RequestHeaderFieldsTooLarge, RetryWith,
        BlockedByParentalControls, UnavailableForLegalReasons, InternalServerError, NotImplemented, BadGateway,
        ServiceUnavailable, GatewayTimeout, HTTPVersionNotSupported, VariantAlsoNegotiates, InsufficientStorage,
        LoopDetected, BandwidthLimitExceeded, NotExtended, NetworkAuthenticationRequired, NetworkReadTimeout,
        NetworkConnectTimeout)


  private def randomHeader = randomStr -> randomStr
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

  def aResponseWith(body: String) = HttpResponse(entity = body)
  def aResponseWith(binaryBody: Array[Byte]) = HttpResponse(entity = binaryBody)
  def aResponseWithoutBody = HttpResponse()

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
  def aRequestWith(binaryBody: Array[Byte]) = HttpRequest(entity = binaryBody)
  def aRequestWithoutBody = HttpRequest()

}