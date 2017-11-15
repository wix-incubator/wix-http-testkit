package com.wix.e2e.http.client.internals

import java.net.URLEncoder

import com.wix.e2e.http.client.drivers.PathBuilderTestSupport
import com.wix.e2e.http.client.drivers.UrlBuilderMatchers._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class PathBuilderTest extends SpecWithJUnit {

  trait ctx extends Scope
  with PathBuilderTestSupport

  "Url building" should {

    "handle context path with single path" in new ctx {
      baseUri.copy(contextRoot = Some(contextRoot)).asUri must beUrl(s"http://${baseUri.host}:${baseUri.port}$contextRoot")
    }

    "handle empty context root" in new ctx {
      baseUri.copy(contextRoot = None).asUri must beUrl(s"http://${baseUri.host}:${baseUri.port}")
      baseUri.copy(contextRoot = Some("")).asUri must beUrl(s"http://${baseUri.host}:${baseUri.port}")
      baseUri.copy(contextRoot = Some("   ")).asUri must beUrl(s"http://${baseUri.host}:${baseUri.port}")
    }

    "handle context path with more than one path" in new ctx {
      baseUri.copy(contextRoot = Some(contextRootWithMultiplePaths)).asUri must beUrl(s"http://${baseUri.host}:${baseUri.port}$contextRootWithMultiplePaths")
    }

    "handle no context and empty relative path" in new ctx {
      baseUri.copy(contextRoot = None).asUriWith("/") must beUrl(s"http://${baseUri.host}:${baseUri.port}")
      baseUri.copy(contextRoot = None).asUriWith("") must beUrl(s"http://${baseUri.host}:${baseUri.port}")
      baseUri.copy(contextRoot = None).asUriWith("    ") must beUrl(s"http://${baseUri.host}:${baseUri.port}")
    }

    "ignore cases in which path and context root are single slash" in new ctx {
      baseUri.copy(contextRoot = Some("/")).asUriWith("/") must beUrl(s"http://${baseUri.host}:${baseUri.port}")
      baseUri.copy(contextRoot = None).asUriWith("/") must beUrl(s"http://${baseUri.host}:${baseUri.port}")
    }

    "allow to append relative path" in new ctx {
      baseUri.copy(contextRoot = None).asUriWith(relativePath) must beUrl(s"http://${baseUri.host}:${baseUri.port}$relativePath")
      baseUri.copy(contextRoot = Some("")).asUriWith(relativePath) must beUrl(s"http://${baseUri.host}:${baseUri.port}$relativePath")
      baseUri.copy(contextRoot = Some("/")).asUriWith(relativePath) must beUrl(s"http://${baseUri.host}:${baseUri.port}$relativePath")
    }

    "allow to append relative path with multiple parts" in new ctx {
      baseUri.copy(contextRoot = None).asUriWith(relativePathWithMultipleParts) must beUrl(s"http://${baseUri.host}:${baseUri.port}$relativePathWithMultipleParts")
    }

    "properly combine context root and relative path" in new ctx {
      baseUri.copy(contextRoot = Some(contextRoot)).asUriWith(relativePath) must beUrl(s"http://${baseUri.host}:${baseUri.port}$contextRoot$relativePath")
      baseUri.copy(contextRoot = Some(contextRootWithMultiplePaths)).asUriWith(relativePathWithMultipleParts) must beUrl(s"http://${baseUri.host}:${baseUri.port}$contextRootWithMultiplePaths$relativePathWithMultipleParts")
    }

    "support context root that doesn't start with /" in new ctx {
      baseUri.copy(contextRoot = Some(contextRoot.stripPrefix("/"))).asUri must beUrl(s"http://${baseUri.host}:${baseUri.port}$contextRoot")
      baseUri.copy(contextRoot = None).asUriWith(relativePath.stripPrefix("/")) must beUrl(s"http://${baseUri.host}:${baseUri.port}$relativePath")
    }

    "support relative path with explicit request params" in new ctx {
      baseUri.copy(contextRoot = None).asUriWith(s"$relativePath?key=val") must beUrl(s"http://${baseUri.host}:${baseUri.port}$relativePath?key=val")
    }

    "support relative path with explicit request escaped params" in new ctx {
      baseUri.copy(contextRoot = None).asUriWith(s"$relativePath?key=val&encoded=$escapedCharacters") must beUrl(s"http://${baseUri.host}:${baseUri.port}$relativePath?key=val&encoded=${URLEncoder.encode(escapedCharacters, "UTF-8")}")
    }
  }
}
