package com.wix.e2e.http.matchers.internal


import akka.http.scaladsl.model.StatusCodes._
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.HttpMessageTestSupport
import org.specs2.mutable.Spec
import org.specs2.specification.Scope


class ResponseStatusMatchersTest extends Spec {

  trait ctx extends Scope with HttpMessageTestSupport


  "ResponseStatusMatchers" should {
      Seq(OK -> beSuccessful, NoContent -> beNoContent, Created -> beSuccessfullyCreated, Accepted -> beAccepted, // 2xx

          Found -> beRedirect, MovedPermanently -> bePermanentlyRedirect, //3xx

          // 4xx
          Forbidden -> beRejected, NotFound -> beNotFound, BadRequest -> beInvalid, PayloadTooLarge -> beRejectedTooLarge,
          Unauthorized -> beUnauthorized, MethodNotAllowed -> beNotSupported, Conflict -> beConflict, PreconditionFailed -> bePreconditionFailed,
          UnprocessableEntity -> beUnprocessableEntity, PreconditionRequired -> bePreconditionRequired, TooManyRequests -> beTooManyRequests,
          RequestHeaderFieldsTooLarge -> beRejectedRequestTooLarge,

          ServiceUnavailable -> beUnavailable, InternalServerError -> beInternalServerError, NotImplemented -> beNotImplemented // 5xx
         ).foreach { case (status, matcherForStatus) =>

        s"match against status ${status.value}" in new ctx {
          aResponseWith( status ) must matcherForStatus
          aResponseWith( randomStatusThatIsNot(status) ) must not( matcherForStatus )
        }
      }

    "allow matching against status code" in new ctx {
      val status = randomStatus
      aResponseWith( status ) must haveStatus(code = status.intValue )
      aResponseWith( status ) must not( haveStatus(code = randomStatusThatIsNot(status).intValue ) )
    }
  }
}