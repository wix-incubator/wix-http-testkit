package com.wix.e2e.http.info

import org.specs2.mutable.Spec

class VersionConsistencyTest extends Spec with VersionConsistencyTestSupport {
  "Version Constant" should {
    "be consistent with version.sbt" in {
      readVersionFromSbtFile must beSome( HttpTestkitVersion )
    }
  }
}
