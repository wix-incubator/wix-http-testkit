import sbt._

object depends {

  private val JacksonVersion = "2.9.8"
  private val AkkaHttpVersion = "10.1.7"
  private val AkkaVersion = "2.5.20"

  def specs2(scalaVersion: String) = specs2DepsFor(specs2VersionFor(scalaVersion))
  def specs2Test(scalaVersion: String) = specs2(scalaVersion).map(_ % Test)

  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.6-SNAP6"

  private def specs2DepsFor(version: String) =
    Seq("org.specs2" %% "specs2-core" % version,
        "org.specs2" %% "specs2-junit" % version,
        "org.specs2" %% "specs2-shapeless" % version,
        "org.specs2" %% "specs2-mock" % version )

  private def specs2VersionFor(scalaVersion: String) = "4.4.1"

  def akkaHttp(scalaVersion: String) =
    Seq("com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
        "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
        "com.typesafe.akka" %% "akka-stream" % AkkaVersion)


  def jackson(scalaVersion: String) = jacksonFor(JacksonVersion, scalaVersion)
  def jacksonFor(version: String, scalaVersion: String) =
    Seq("com.fasterxml.jackson.core" % "jackson-databind" % version,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % version,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % version,
        "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % version,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % version,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % version ) 

  val joda = Seq("joda-time" % "joda-time" % "2.10.1",
                 "org.joda" % "joda-convert" % "2.1.2" )

  val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.1.1"

  val reflections = "org.reflections" % "reflections" % "0.9.11"

  val jsr305 = "com.google.code.findbugs" % "jsr305" % "3.0.2"

  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.25"
}
