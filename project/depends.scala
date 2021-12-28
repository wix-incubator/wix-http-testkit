import sbt._

object depends {

  private val JacksonVersion = "2.13.1"
  private val AkkaHttpVersion = "10.2.7"
  private val AkkaVersion = "2.6.18"
  private val Specs2Version = "4.13.1"

  val specs2 =
    Seq("org.specs2" %% "specs2-core" % Specs2Version,
        "org.specs2" %% "specs2-junit" % Specs2Version,
        "org.specs2" %% "specs2-shapeless" % Specs2Version,
        "org.specs2" %% "specs2-mock" % Specs2Version )
  val specs2Test = specs2.map(_ % Test)

  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.10"

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

  val joda = Seq("joda-time" % "joda-time" % "2.10.13",
                 "org.joda" % "joda-convert" % "2.2.2" )

  val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.3.0"

  val reflections = "org.reflections" % "reflections" % "0.10.2"

  val jsr305 = "com.google.code.findbugs" % "jsr305" % "3.0.2"

  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.32"
}
