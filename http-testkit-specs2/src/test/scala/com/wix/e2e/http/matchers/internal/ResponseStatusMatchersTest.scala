package com.wix.e2e.http.matchers.internal


import akka.http.scaladsl.model.StatusCodes._
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.HttpResponseTestSupport
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class ResponseStatusMatchersTest extends SpecWithJUnit {

  trait ctx extends Scope with HttpResponseTestSupport


  "ResponseStatusMatchers" should {
    "test all matchers" in new ctx {
      Seq(OK -> beSuccessful, NoContent -> beNoContent, Created -> beSuccessfullyCreated, Accepted -> beAccepted, // 2xx

          Found -> beRedirect, MovedPermanently -> bePermanentlyRedirect, //3xx

          // 4xx
          Forbidden -> beRejected, NotFound -> beNotFound, BadRequest -> beInvalid, RequestEntityTooLarge -> beRejectedTooLarge,
          Unauthorized -> beUnauthorized, MethodNotAllowed -> beNotSupported, Conflict -> beConflict, PreconditionFailed -> bePreconditionFailed,
          UnprocessableEntity -> beUnprocessableEntity, PreconditionRequired -> bePreconditionRequired, TooManyRequests -> beTooManyRequests,

          ServiceUnavailable -> beUnavailable, InternalServerError -> beInternalServerError, NotImplemented -> beNotImplemented // 5xx
         ).foreach { case (status, matcherForStatus) =>

        aResponseWith( status ) must matcherForStatus
        aResponseWith( randomStatusThatIsNot(status) ) must not( matcherForStatus )
      }
      ok
    }
  }
}