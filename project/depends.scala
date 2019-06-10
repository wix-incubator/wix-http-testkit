import sbt._

object depends {

  private val JacksonVersion = "2.9.9"
  private val AkkaHttpVersion = "10.1.8"
  private val AkkaVersion = "2.5.23"
  private val Specs2Version = "4.5.1"

  val specs2 =
    Seq("org.specs2" %% "specs2-core" % Specs2Version,
        "org.specs2" %% "specs2-junit" % Specs2Version,
        "org.specs2" %% "specs2-shapeless" % Specs2Version,
        "org.specs2" %% "specs2-mock" % Specs2Version )
  val specs2Test = specs2.map(_ % Test)

  val scalaTest = "org.scalatest" %% "scalatest" % "3.1.0-SNAP12"

  val akkaHttp =
    Seq("com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
        "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
        "com.typesafe.akka" %% "akka-stream" % AkkaVersion)

  val jackson = jacksonFor(JacksonVersion)
  
  private def jacksonFor(version: String) =
    Seq("com.fasterxml.jackson.core" % "jackson-databind" % version,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % version,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % version,
        "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % version,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % version,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % version ) 

  val joda = Seq("joda-time" % "joda-time" % "2.10.2",
                 "org.joda" % "joda-convert" % "2.2.1" )

  val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.2.0"

  val reflections = "org.reflections" % "reflections" % "0.9.11"

  val jsr305 = "com.google.code.findbugs" % "jsr305" % "3.0.2"

  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.26"
}
