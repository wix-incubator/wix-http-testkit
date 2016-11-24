import sbt._
import Keys._

object depends {


  def specs2 = Seq(
    "org.specs2" %% "specs2-core" % "3.8.6" % "test",
    "org.specs2" %% "specs2-junit" % "3.8.6" % "test",
    "org.specs2" %% "specs2-matcher-extra" % "3.8.6" % "test"
  )

  def akkaHttp = Seq(
    "com.typesafe.akka" %% "akka-http" % "10.0.0",
    "com.typesafe.akka" %% "akka-actor" % "2.4.9"
  )

  def wixFWDependenciesFor(version: String) = Seq(
    "com.wixpress.hoopoe" % "hoopoe-http-test-kit" % version,
    "com.wixpress.hoopoe" % "hoopoe-utest" % version % "test"
  )

  val wixArtifactory = Seq("libs-releases" at "http://repo.dev.wix/artifactory/libs-releases",
                      "libs-snapshots" at "http://repo.dev.wix/artifactory/libs-snapshots")
  /*

    resolvers := Seq( "libs-releases" at "http://repo.dev.wix/artifactory/libs-releases",
    "libs-snapshots" at "http://repo.dev.wix/artifactory/libs-snapshots"),


      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-http" % "10.0.0",
        "com.typesafe.akka" %% "akka-actor" % "2.4.9",
        "com.wixpress.hoopoe" % "hoopoe-http-test-kit" % fwVersion,
        "org.specs2" %% "specs2-core" % "3.8.6" % "test",
        "org.specs2" %% "specs2-junit" % "3.8.6" % "test",
        "org.specs2" %% "specs2-matcher-extra" % "3.8.6" % "test",
        "com.wixpress.hoopoe" % "hoopoe-utest" % fwVersion % "test"),

    def compiler(scalaVersion: String) = Seq("org.scala-lang" % "scala-compiler" % scalaVersion)

  def reflect(scalaVersion: String) = Seq("org.scala-lang" % "scala-reflect" % scalaVersion)

  def scalaz(scalazVersion: String) =
    Seq("org.scalaz"        %% "scalaz-core",
        "org.scalaz"        %% "scalaz-effect",
        "org.scalaz"        %% "scalaz-concurrent").map(_ % scalazVersion)

   */



  /*
lazy val resolvers =
    Seq(updateOptions := updateOptions.value.withCachedResolution(true)) ++ {
      sbt.Keys.resolvers ++=
      Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.sonatypeRepo("snapshots"),
        Resolver.typesafeIvyRepo("releases"),
        "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases")
    }
   */

}