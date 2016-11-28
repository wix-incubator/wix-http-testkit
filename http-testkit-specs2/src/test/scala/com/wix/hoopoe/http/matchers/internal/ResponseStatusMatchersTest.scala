package com.wix.hoopoe.http.matchers.internal


import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpResponse, StatusCode}
import com.wix.hoopoe.http.matchers.ResponseMatchers._
import org.specs2.mutable.SpecWithJUnit

import scala.util.Random


class ResponseStatusMatchersTest extends SpecWithJUnit {

  "ResponseStatusMatchers" should {

    "test all matchers" in {
      Seq(OK -> beSuccessful, NoContent -> beNoContent, Created -> beSuccessfullyCreated, Accepted -> beAccepted, // 2xx

          Found -> beRedirect, MovedPermanently -> bePermanentlyRedirect, //3xx

          // 4xx
          Forbidden -> beRejected, NotFound -> beNotFound, BadRequest -> beInvalid, RequestEntityTooLarge -> beRejectedTooLarge,
          Unauthorized -> beUnauthorized, MethodNotAllowed -> beNotSupported, Conflict -> beConflict, PreconditionFailed -> bePreconditionFailed,
          UnprocessableEntity -> beUnprocessableEntity, PreconditionRequired -> bePreconditionRequired, TooManyRequests -> beTooManyRequests,

          ServiceUnavailable -> beUnavailable, InternalServerError -> beInternalServerError, NotImplemented -> beNotImplemented // 5xx
         ).foreach { case (status, matcherForStatus) =>

        HttpResponse(status) must matcherForStatus
        HttpResponse(randomStatusThatIsNot(status)) must not( matcherForStatus )
      }
      ok
    }
  }

  private def randomStatusThatIsNot(status: StatusCode): StatusCode =
    Random.shuffle(Seq(Continue, SwitchingProtocols, Processing, OK, Created, Accepted, NonAuthoritativeInformation,
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
                .filterNot(_ == status))
          .head
}