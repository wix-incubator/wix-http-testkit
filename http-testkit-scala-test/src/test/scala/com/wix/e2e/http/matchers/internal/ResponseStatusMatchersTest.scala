package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.StatusCodes._
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpMessageTestSupport
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class ResponseStatusMatchersTest extends AnyWordSpec {

  trait ctx extends HttpMessageTestSupport


  "ResponseStatusMatchers" should {
      Seq(OK -> beSuccessful, NoContent -> beNoContent, Created -> beSuccessfullyCreated, Accepted -> beAccepted, // 2xx

          Found -> beRedirect, MovedPermanently -> bePermanentlyRedirect, //3xx

          // 4xx
          Forbidden -> beRejected, NotFound -> beNotFound, BadRequest -> beInvalid, PayloadTooLarge -> beRejectedTooLarge,
          Unauthorized -> beUnauthorized, MethodNotAllowed -> beNotSupported, Conflict -> beConflict, PreconditionFailed -> bePreconditionFailed,
          UnprocessableEntity -> beUnprocessableEntity, PreconditionRequired -> bePreconditionRequired, TooManyRequests -> beTooManyRequests,

          ServiceUnavailable -> beUnavailable, InternalServerError -> beInternalServerError, NotImplemented -> beNotImplemented // 5xx
         ).foreach { case (status, matcherForStatus) =>

        s"match against status ${status.value}" in new ctx {
          aResponseWith( status ) should matcherForStatus
          aResponseWith( randomStatusThatIsNot(status) ) should not( matcherForStatus )
        }
      }

    "allow matching against status code" in new ctx {
      val status = randomStatus
      aResponseWith( status ) should haveStatus(code = status.intValue )
      aResponseWith( status ) should not( haveStatus(code = randomStatusThatIsNot(status).intValue ) )
    }
  }
}