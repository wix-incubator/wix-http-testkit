import sbt._

object depends {

  private val JacksonVersion = "2.9.1"

  def specs2(scalaVersion: String) = specs2DepsFor(specs2VersionFor(scalaVersion))
  def specs2Test(scalaVersion: String) = specs2(scalaVersion).map(_ % Test)

  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4"

  val scalaMock = "org.scalamock" %% "scalamock" % "4.0.0" % Test

  private def specs2DepsFor(version: String) =
    Seq("org.specs2" %% "specs2-core" % version,
        "org.specs2" %% "specs2-junit" % version,
      "org.specs2" %% "specs2-shapeless" % version,
        "org.specs2" %% "specs2-mock" % version )

  private def specs2VersionFor(scalaVersion: String) = "4.0.2"

//    if ( scalaVersion.startsWith("2.13") ) "4.0.2" else "3.8.6"


  def akkaHttp(scalaVersion: String) = "com.typesafe.akka" %% "akka-http" % "10.0.10" // missing 2.13

  def jackson(scalaVersion: String) = jacksonFor(JacksonVersion, scalaVersion)
  def jacksonFor(version: String, scalaVersion: String) =
    Seq("com.fasterxml.jackson.core" % "jackson-databind" % version,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % version,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % version,
        "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % version,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % version,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % version ) // missing 2.13

  val joda = Seq("joda-time" % "joda-time" % "2.9.9",
                 "org.joda" % "joda-convert" % "1.9.2" )

  val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.6"

  val reflections = "org.reflections" % "reflections" % "0.9.10"

  val jsr305 = "com.google.code.findbugs" % "jsr305" % "3.0.2"

  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.25"
}