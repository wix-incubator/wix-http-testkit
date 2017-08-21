import sbt._

object depends {

  private val Specs2Version = "3.8.6"
  private val JacksonVersion = "2.9.0"

  val specs2 =
    Seq("org.specs2" %% "specs2-core" % Specs2Version,
        "org.specs2" %% "specs2-junit" % Specs2Version,
        "org.specs2" %% "specs2-mock" % Specs2Version )

  val akkaHttp =
    Seq("com.typesafe.akka" %% "akka-http" % "10.0.9"/*,
        "com.typesafe.akka" %% "akka-actor" % "2.4.20"*/)

  val jackson = jacksonFor(JacksonVersion)
  val jackson2_6 = jacksonFor("2.6.7")
  def jacksonFor(version: String) =
    Seq("com.fasterxml.jackson.core" % "jackson-databind" % version,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % version,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % version,
        "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % version,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % version,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % version )

  val joda = Seq("joda-time" % "joda-time" % "2.9.6",
                 "org.joda" % "joda-convert" % "1.8.1" )

  val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.6"

  val reflections = "org.reflections" % "reflections" % "0.9.11"

  val jsr305 = "com.google.code.findbugs" % "jsr305" % "3.0.2"

  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.25"
}