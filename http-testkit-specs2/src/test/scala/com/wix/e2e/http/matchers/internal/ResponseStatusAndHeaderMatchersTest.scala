package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.StatusCodes.{Found, MovedPermanently}
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.{HttpResponseTestSupport, MatchersTestSupport}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class ResponseStatusAndHeaderMatchersTest extends SpecWithJUnit with MatchersTestSupport {

  trait ctx extends Scope with HttpResponseTestSupport

  "ResponseStatusAndHeaderMatchers" should {

    "match against a response that is temporarily redirected to url" in new ctx {
      aRedirectResponseTo(url) must beRedirectedTo(url)
      aRedirectResponseTo(url) must not( beRedirectedTo(anotherUrl) )
      aRedirectResponseTo(url).copy(status = randomStatusThatIsNot(Found)) must not( beRedirectedTo(url) )
    }

    "match against a response that is permanently redirected to url" in new ctx {
      aPermanentlyRedirectResponseTo(url) must bePermanentlyRedirectedTo(url)
      aPermanentlyRedirectResponseTo(url) must not( bePermanentlyRedirectedTo(anotherUrl) )
      aPermanentlyRedirectResponseTo(url).copy(status = randomStatusThatIsNot(MovedPermanently)) must not( bePermanentlyRedirectedTo(url) )
    }
  }
}
