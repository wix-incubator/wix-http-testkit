import depends._
import compiler_helper._

lazy val publishSettings = Seq(
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if ( version.value.trim.endsWith( "SNAPSHOT" ) )
      Some( "snapshots" at nexus + "content/repositories/snapshots" )
    else
      Some( "releases" at nexus + "service/local/staging/deploy/maven2" )
  },
  publishMavenStyle := true,
  pomExtra in ThisBuild :=
    <scm>
      <url>git@github.com:wix/wix-http-testkit.git</url>
      <connection>scm:git:git@github.com:wix/wix-http-testkit.git</connection>
    </scm>
      <developers>
        <developer>
          <id>noama</id>
          <name>Noam Almog</name>
          <email>noamal@gmail.com</email>
          <organization>Wix</organization>
        </developer>
      </developers>
)

lazy val compileOptions = Seq(
  scalaVersion := "2.13.0",
  crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0"),
  scalacOptions ++= compilerFlagsFor(scalaVersion.value),
)

lazy val noPublish = Seq( publish := {}, publishLocal := {}, publishArtifact := false )

lazy val baseSettings =
  publishSettings ++
    compileOptions ++
    Seq(
      organization := "com.wix",
      homepage := Some( url( "https://github.com/wix-private/http-testkit" ) ),
      licenses := Seq( "MIT" -> url( "https://opensource.org/licenses/MIT" ) )
    )

lazy val httpTestkitTestCommons =
  (project in file("http-testkit-test-commons"))
    .settings(Seq(
      name := "http-testkit-test-commons",
      description := "Commonly used test utilities"
    ) ++ baseSettings ++ noPublish)

lazy val httpTestkitCore =
  (project in file("http-testkit-core"))
    .settings(crossBuildMultipleSourcesOptions)
    .settings(Seq(
      name := "http-testkit-core",
      libraryDependencies ++= joda ++ specs2Test ++ akkaHttp :+ scalaXml :+ reflections :+ jsr305 :+ slf4jApi,
      description := "Commonly used util code also client and server interfaces",
    ) ++ baseSettings)
    .dependsOn(httpTestkitTestCommons % Test)

lazy val httpTestkitClient =
  (project in file("http-testkit-client"))
    .settings(Seq(
      name := "http-testkit-client",
      libraryDependencies ++= specs2 ,
      description := "All code related to REST client, blocking and non-blocking"
    ) ++ baseSettings)
    .dependsOn(httpTestkitCore, httpTestkitSpecs2, httpTestkitTestCommons % Test, httpTestkitMarshallerJackson % Test)

lazy val httpTestkitServer =
  (project in file("http-testkit-server"))
    .settings(Seq(
      name := "http-testkit-server",
      description := "Server implementations - stub and mock"
    ) ++ baseSettings)
    .dependsOn(httpTestkitCore)

lazy val httpTestkitSpecs2 =
  (project in file("http-testkit-specs2"))
    .settings(Seq(
      name := "http-testkit-specs2",
      libraryDependencies ++= specs2,
      description := "Specs2 Matcher suites - Request and Response."
    ) ++ baseSettings)
    .dependsOn(httpTestkitCore, httpTestkitTestCommons % Test, httpTestkitMarshallerJackson % Test)

lazy val httpTestkitScalaTest =
  (project in file("http-testkit-scala-test"))
    .settings(Seq(
      name := "http-testkit-scalatest",
      libraryDependencies ++= Seq(scalaTest),
      description := "Scala Test Matcher suites - Request and Response."
    ) ++ baseSettings)
    .dependsOn(httpTestkitCore, httpTestkitTestCommons % Test, httpTestkitMarshallerJackson % Test)

lazy val httpTestkitMarshallerJackson =
  (project in file("http-testkit-marshaller-jackson"))
    .settings(Seq(
      name := "http-testkit-marshaller-jackson",
      libraryDependencies ++= jackson ++ specs2,
      description := "Marshaller implementation - jackson"
    ) ++ baseSettings)
    .dependsOn(httpTestkitCore, httpTestkitTestCommons % Test)

lazy val httpTestkit =
  (project in file("http-testkit"))
    .settings(Seq(
      name := "Http Testkit",
      description := "Main module, contain factories but no implementation."
    ) ++ baseSettings)
    .dependsOn(httpTestkitClient, httpTestkitServer, httpTestkitSpecs2)

lazy val httpTestkitContractTests =
  (project in file("contract-tests/http-testkit-contract-tests"))
    .settings(crossBuildMultipleSourcesOptions)
    .settings(Seq(
      name := "http-testkit-contract-tests",
      libraryDependencies ++= specs2Test,
      description := "Contract tests for both client and server"
    ) ++ baseSettings ++ noPublish)
    .dependsOn(httpTestkit, httpTestkitMarshallerJackson, httpTestkitTestCommons % Test)

lazy val httpTestkitContractTestsCustomMarshaller =
  (project in file("contract-tests/marshaller-contract-tests/http-testkit-contract-tests-custom-marshaller"))
    .settings(Seq(
      name := "http-testkit-contract-tests-custom-marshaller",
      libraryDependencies ++= specs2Test,
      description := "Contract tests for marshaller: cover cases which custom marshaller exists on classpath"
    ) ++ baseSettings ++ noPublish)
    .dependsOn(httpTestkit, httpTestkitTestCommons % Test)

lazy val httpTestkitContractTestsNoCustomMarshaller =
  (project in file("contract-tests/marshaller-contract-tests/http-testkit-contract-tests-no-custom-marshaller"))
    .settings(Seq(
      name := "http-testkit-contract-tests-no-custom-marshaller",
      libraryDependencies ++= specs2Test,
      description := "Contract tests for marshaller: cover cases which no custom marshaller exists on classpath"
    ) ++ baseSettings ++ noPublish)
    .dependsOn(httpTestkit, httpTestkitTestCommons % Test)

lazy val httpTestkitContractTestsDualMarshallers =
  (project in file("contract-tests/marshaller-contract-tests/http-testkit-contract-tests-dual-marshallers"))
    .settings(Seq(
      name := "http-testkit-contract-tests-dual-marshallers",
      libraryDependencies ++= specs2Test,
      description := "Contract tests for marshaller: cover cases which custom marshaller and default marshaller exists on classpath"
    ) ++ baseSettings ++ noPublish)
    .dependsOn(httpTestkit, httpTestkitMarshallerJackson, httpTestkitTestCommons % Test)

lazy val httpTestkitContractTestsMalformedMarshaller =
  (project in file("contract-tests/marshaller-contract-tests/http-testkit-contract-tests-malformed-marshaller"))
    .settings(Seq(
      name := "http-testkit-contract-tests-malformed-marshaller",
      libraryDependencies ++= specs2Test,
      description := "Contract tests for marshaller"
    ) ++ baseSettings ++ noPublish)
    .dependsOn(httpTestkitCore)

lazy val httpTestkitExamples =
  (project in file("examples"))
    .settings(Seq(
      name := "http-testkit-examples",
      libraryDependencies ++= specs2Test,
      description := "Testkit Examples"
    ) ++ baseSettings ++ noPublish)
    .dependsOn(httpTestkit)

//lazy val httpTestkitExamples =
//  Project(
//    id = "examples",
//    base = file( "examples" ),
//    settings = Seq(
//      name := "examples",
//      libraryDependencies ++= specs2.map(_ % "test") ,
//      description :=
//        "Some crap i need to describe the library"
//    ) ++ baseSettings
//  ).dependsOn(wixHttpTestkit)

lazy val marshallerContractTests =
  (project in file("contract-tests/marshaller-contract-tests"))
    .settings(Seq(name:= "Wix Http Testkit Marshaller Contract Tests") ++ baseSettings ++ noPublish)
    .aggregate(httpTestkitContractTestsCustomMarshaller, httpTestkitContractTestsNoCustomMarshaller,
               httpTestkitContractTestsDualMarshallers)


lazy val contractTests =
  (project in file("contract-tests"))
    .settings(Seq(name:= "Wix Http Testkit Contract Tests") ++ baseSettings ++ noPublish)
    .aggregate(httpTestkitContractTests, marshallerContractTests)

lazy val root =
  (project in file("."))
    .settings(Seq(name:= "Wix Http Testkit Modules") ++ baseSettings ++ noPublish)
    .aggregate(httpTestkitTestCommons,
               httpTestkitCore, httpTestkitClient, httpTestkitServer, httpTestkitSpecs2, httpTestkitScalaTest, httpTestkit, httpTestkitMarshallerJackson,
               contractTests,
               httpTestkitExamples)
