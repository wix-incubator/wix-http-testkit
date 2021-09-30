package com.wix.e2e.http.info

import java.io.File
import java.nio.file.Files

import scala.jdk.CollectionConverters._

trait VersionConsistencyTestSupport {

  def readVersionFromSbtFile =
    Files.readAllLines(new File("./version.sbt").toPath).asScala
      .find( findLineContainingVersion )
      .map( parseVersionFromSbt )

  def parseVersionFromSbt(line: String) =
    line.substring( line.indexOf('"') + 1, line.lastIndexOf('"'))

  def findLineContainingVersion(line: String) =
    line.contains("ThisBuild / version")
}
