import sbt._

object depends {

  private val Specs2Version = "3.8.6"
  private val JacksonVersion = "2.9.0"

  def specs2 =
    Seq("org.specs2" %% "specs2-core" % Specs2Version,
        "org.specs2" %% "specs2-junit" % Specs2Version,
        "org.specs2" %% "specs2-mock" % Specs2Version )

  def akkaHttp =
    Seq("com.typesafe.akka" %% "akka-http" % "10.0.9"/*,
        "com.typesafe.akka" %% "akka-actor" % "2.4.19"*/)

  def jackson =
    Seq("com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % JacksonVersion,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % JacksonVersion,
        "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % JacksonVersion,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % JacksonVersion,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion )

  def joda = Seq("joda-time" % "joda-time" % "2.9.6",
                 "org.joda" % "joda-convert" % "1.8.1" )

  def scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
}