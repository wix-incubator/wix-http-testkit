package com.wix.e2e.http.info

import java.io.File
import java.nio.file.Files

import org.specs2.mutable.Spec

import scala.collection.JavaConverters._

class VersionConsistencyTest extends Spec with VersionConsistencyTestSupport {
  "Version Constant" should {
    "be consistent with version.sbt" in {
      readVersionFromSbtFile must beSome( HttpTestkitVersion )
    }
  }
}

trait VersionConsistencyTestSupport {

  def readVersionFromSbtFile =
    Files.readAllLines(new File("./version.sbt").toPath).asScala
         .find( findLineContainingVersion )
         .map( parseVersionFromSbt )

  def parseVersionFromSbt(line: String) =
    line.substring( line.indexOf('"') + 1, line.lastIndexOf('"'))

  def findLineContainingVersion(line: String) =
    line.contains("version in ThisBuild")
}