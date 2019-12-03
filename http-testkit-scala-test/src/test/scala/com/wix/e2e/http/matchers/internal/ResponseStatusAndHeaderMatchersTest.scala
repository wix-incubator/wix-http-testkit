package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.StatusCodes.{Found, MovedPermanently}
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers.drivers.HttpResponseFactory._
import com.wix.e2e.http.matchers.drivers.{HttpMessageTestSupport, MatchersTestSupport}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class ResponseStatusAndHeaderMatchersTest extends AnyWordSpec with MatchersTestSupport {

  trait ctx extends HttpMessageTestSupport

  "ResponseStatusAndHeaderMatchers" should {

    "match against a response that is temporarily redirected to url" in new ctx {
      aRedirectResponseTo(url) should beRedirectedTo(url)
      aRedirectResponseTo(url) should not( beRedirectedTo(anotherUrl) )
      aRedirectResponseTo(url).copy(status = randomStatusThatIsNot(Found)) should not( beRedirectedTo(url) )
    }

    "match against a response that is permanently redirected to url" in new ctx {
      aPermanentlyRedirectResponseTo(url) should bePermanentlyRedirectedTo(url)
      aPermanentlyRedirectResponseTo(url) should not( bePermanentlyRedirectedTo(anotherUrl) )
      aPermanentlyRedirectResponseTo(url).copy(status = randomStatusThatIsNot(MovedPermanently)) should not( bePermanentlyRedirectedTo(url) )
    }

    "match against url params even if params has a different order" in new ctx {
      aRedirectResponseTo(s"$url?param1=val1&param2=val2") should beRedirectedTo(s"$url?param2=val2&param1=val1")
      aPermanentlyRedirectResponseTo(s"$url?param1=val1&param2=val2") should bePermanentlyRedirectedTo(s"$url?param2=val2&param1=val1")
    }

    "match will fail for different protocol" in new ctx {
      aRedirectResponseTo(s"http://example.com") should not( beRedirectedTo(s"https://example.com") )
      aPermanentlyRedirectResponseTo(s"http://example.com") should not( bePermanentlyRedirectedTo(s"https://example.com") )
    }

    "match will fail for different host and port" in new ctx {
      aRedirectResponseTo(s"http://example.com") should not( beRedirectedTo(s"http://example.org") )
      aRedirectResponseTo(s"http://example.com:99") should not( beRedirectedTo(s"http://example.com:81") )
      aPermanentlyRedirectResponseTo(s"http://example.com") should not( bePermanentlyRedirectedTo(s"http://example.org") )
      aPermanentlyRedirectResponseTo(s"http://example.com:99") should not( bePermanentlyRedirectedTo(s"http://example.com:81") )
    }

    "port 80 is removed by akka http" in new ctx {
      aRedirectResponseTo(s"http://example.com:80") should beRedirectedTo(s"http://example.com")
      aPermanentlyRedirectResponseTo(s"http://example.com:80") should bePermanentlyRedirectedTo(s"http://example.com")
    }

    "match will fail for different path" in new ctx {
      aRedirectResponseTo(s"http://example.com/path1") should not( beRedirectedTo(s"http://example.com/path2") )
      aPermanentlyRedirectResponseTo(s"http://example.com/path1") should not( bePermanentlyRedirectedTo(s"http://example.org/path2") )
    }

    "match will fail for different hash fragment" in new ctx {
      aRedirectResponseTo(s"http://example.com/path#fragment") should not( beRedirectedTo(s"http://example.com/path#anotherFxragment") )
      aPermanentlyRedirectResponseTo(s"http://example.com/path#fragment") should not( bePermanentlyRedirectedTo(s"http://example.com/path#anotherFxragment") )
    }

    "failure message in case response does not have location header" in new ctx {
      failureMessageFor(beRedirectedTo(url), matchedOn = aRedirectResponseWithoutLocationHeader) shouldBe
        "Response does not contain Location header."
      failureMessageFor(bePermanentlyRedirectedTo(url), matchedOn = aPermanentlyRedirectResponseWithoutLocationHeader) shouldBe
        "Response does not contain Location header."
    }

    "failure message in case trying to match against a malformed url" in new ctx {
      failureMessageFor(beRedirectedTo(malformedUrl), matchedOn = aRedirectResponseTo(url)) shouldBe
        s"Matching against a malformed url: [$malformedUrl]."
      failureMessageFor(bePermanentlyRedirectedTo(malformedUrl), matchedOn = aPermanentlyRedirectResponseTo(url)) shouldBe
        s"Matching against a malformed url: [$malformedUrl]."
    }

    "failure message in case response have different urls should show the actual url and the expected url" in new ctx {
      failureMessageFor(beRedirectedTo(url), matchedOn = aRedirectResponseTo(s"$url?param1=val1")) shouldBe
        s"""Response is redirected to a different url:
           |actual:   $url?param1=val1
           |expected: $url
           |""".stripMargin
      failureMessageFor(bePermanentlyRedirectedTo(url), matchedOn = aPermanentlyRedirectResponseTo(s"$url?param1=val1")) shouldBe
        s"""Response is redirected to a different url:
           |actual:   $url?param1=val1
           |expected: $url
           |""".stripMargin
    }
  }
}
