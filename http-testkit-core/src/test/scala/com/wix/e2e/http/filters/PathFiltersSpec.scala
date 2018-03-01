package com.wix.e2e.http.filters

import akka.http.scaladsl.model.{HttpRequest, Uri}
import org.specs2.mutable.Spec

class PathFiltersSpec extends Spec with PathFilters {

  "forPath" should {
    "match two exact paths" in {
      matchPaths("/api/users/123423/remove", "/api/users/123423/remove") must beTrue
      matchPaths("api/users/123423/remove", "/api/users/123423/remove") must beTrue
      matchPaths("api/users/123423/remove", "api/users/123423/remove/") must beTrue
      matchPaths("/api/users/123423/", "/api/users/123423/remove") must beFalse
      matchPaths("/api/users/123423/remove", "/api/users/123423/") must beFalse
    }

    "match two paths with wildcard" in {
      matchPaths("/api/users/123423/remove", "/api/users/*/remove") must beTrue
      matchPaths("/api/users/123423/remove", "*/users/123423/remove") must beTrue
      matchPaths("/api/users/123423/remove", "/api/users/*/*") must beTrue
      matchPaths("/api/users/123423/remove", "/api/users/*") must beFalse
      matchPaths("/api/users/123423/remove", "/api/*/remove") must beFalse
    }

    "match two paths with multi-wildcard" in {
      matchPaths("/api/users/123423/remove", "/api/users/**/") must beTrue
      matchPaths("/api/users/123423/remove", "**/123423/remove") must beTrue
      matchPaths("/api/users/123423/remove", "/*/**/remove") must beTrue
      matchPaths("/api/users/123423/remove", "/**/update") must beFalse
      matchPaths("/api/users/123423/remove", "/**/123/**") must beFalse
    }
  }

  def matchPaths(actual: String, expected: String): Boolean = {
    val req = HttpRequest(uri = Uri(actual))
    whenPathIs(expected)(req)
  }
}
