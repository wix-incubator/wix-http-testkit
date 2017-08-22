import depends._

//lazy val javaRuntimeVersion = settingKey[ Double ]( "The JVM runtime version (e.g. 1.8)" )

resolvers += Resolver.url("bintray-sbt-plugin-releases", url("https://dl.bintray.com/content/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

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
      <url>git@github.com:wix-private/http-testkit.git</url>
      <connection>scm:git:git@github.com:wix-private/http-testkit.git</connection>
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
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.11", "2.12.3"),
//  sbtVersion in Global := "1.0.0",
//  scalaCompilerBridgeSource := {
//    val sv = appConfiguration.value.provider.id.version
//    ("org.scala-sbt" % "compiler-interface" % sv % "component").sources
//  },
//  scalaOrganization in ThisBuild := "org.typelevel",
//  javaRuntimeVersion := System.getProperty( "java.vm.specification.version" ).toDouble,
//  crossScalaVersions := ( javaRuntimeVersion.value match {
//    case v if v >= 1.8 => Seq( "2.11.8", "2.12.1" )
//    case _             => Seq( "2.11.8" )
//  } ),
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xfatal-warnings"
  )
)

lazy val noPublish = Seq( publish := {}, publishLocal := {}, publishArtifact := false )

lazy val baseSettings =
  publishSettings ++
//    releaseSettings ++
    compileOptions ++
    Seq(
      organization := "com.wix",
      homepage := Some( url( "https://github.com/wix-private/http-testkit" ) ),
      licenses := Seq( "MIT" -> url( "https://opensource.org/licenses/MIT" ) )
    )

lazy val httpTestkitTestCommons =
  Project(
    id = "http-testkit-test-commons",
    base = file( "http-testkit-test-commons" ),
    settings = Seq(
      name := "http-testkit-test-commons",
      description := "Commonly used test utilities"
    ) ++ baseSettings ++ noPublish
  )

lazy val httpTestkitCore =
  Project(
    id = "http-testkit-core",
    base = file( "http-testkit-core" ),
    settings = Seq(
      name := "http-testkit-core",
      libraryDependencies ++= akkaHttp ++ joda ++ specs2.map(_ % Test) :+ scalaXml :+ reflections :+ jsr305 :+ slf4jApi,
      description := "Commonly used util code also client and server interfaces"
    ) ++ baseSettings
  ).dependsOn(httpTestkitTestCommons % Test)

lazy val httpTestkitClient =
  Project(
    id = "http-testkit-client",
    base = file( "http-testkit-client" ),
    settings = Seq(
      name := "http-testkit-client",
      libraryDependencies ++= specs2.map(_ % Test) ,
      description := "All code related to REST client, blocking and non-blocking"
    ) ++ baseSettings
  ).dependsOn(httpTestkitCore, httpTestkitSpecs2, httpTestkitTestCommons % Test, httpTestkitMarshallerJackson % Test)

lazy val httpTestkitServer =
  Project(
    id = "http-testkit-server",
    base = file( "http-testkit-server" ),
    settings = Seq(
      name := "http-testkit-server",
      description := "Server implementations - stub and mock"
    ) ++ baseSettings
  ).dependsOn(httpTestkitCore)

lazy val httpTestkitSpecs2 =
  Project(
    id = "http-testkit-specs2",
    base = file( "http-testkit-specs2" ),
    settings = Seq(
      name := "http-testkit-specs2",
      libraryDependencies ++= specs2,
      description := "Specs2 Matcher suites - Request and Response."
    ) ++ baseSettings
  ).dependsOn(httpTestkitCore, httpTestkitTestCommons % Test, httpTestkitMarshallerJackson % Test)

lazy val httpTestkitMarshallerJackson =
  Project(
    id = "http-testkit-marshaller-jackson",
    base = file( "http-testkit-marshaller-jackson" ),
    settings = Seq(
      name := "http-testkit-marshaller-jackson",
      libraryDependencies ++= jackson ++ specs2,
      description := "Marshaller implementation - jackson"
    ) ++ baseSettings
  ).dependsOn(httpTestkitCore, httpTestkitTestCommons % Test)

lazy val httpTestkit =
  Project(
    id = "http-testkit",
    base = file( "http-testkit" ),
    settings = Seq(
      name := "Http Testkit",
      description := "Main module, contain factories but no implementation."
    ) ++ baseSettings
  ).dependsOn(httpTestkitClient, httpTestkitServer, httpTestkitSpecs2)

lazy val httpTestkitContractTests =
  Project(
    id = "http-testkit-contract-tests",
    base = file( "http-testkit-contract-tests" ),
    settings = Seq(
      name := "http-testkit-contract-tests",
      libraryDependencies ++= specs2.map(_ % "test") ,
      description := "Contract tests for both client and server"
    ) ++ baseSettings ++ noPublish
  ).dependsOn(httpTestkit, httpTestkitMarshallerJackson, httpTestkitTestCommons % Test)

lazy val httpTestkitContractTestsCustomMarshaller =
  Project(
    id = "http-testkit-contract-tests-custom-marshaller",
    base = file( "http-testkit-contract-tests-custom-marshaller" ),
    settings = Seq(
      name := "http-testkit-contract-tests-custom-marshaller",
      libraryDependencies ++= specs2.map(_ % "test") ,
      description := "Contract tests for both client and server"
    ) ++ baseSettings ++ noPublish
  ).dependsOn(httpTestkit, httpTestkitTestCommons % Test)

lazy val httpTestkitContractTestsNoCustomMarshaller =
  Project(
    id = "http-testkit-contract-tests-no-custom-marshaller",
    base = file( "http-testkit-contract-tests-no-custom-marshaller" ),
    settings = Seq(
      name := "http-testkit-contract-tests-no-custom-marshaller",
      libraryDependencies ++= specs2.map(_ % "test") ,
      description := "Contract tests for both client and server"
    ) ++ baseSettings ++ noPublish
  ).dependsOn(httpTestkit, httpTestkitTestCommons % Test)

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


lazy val root =
  Project(
    id = "wix-http-testkit-modules",
    base = file( "." ),
    settings = Seq(name:= "Wix Http Testkit Modules") ++ baseSettings ++ noPublish
  ).aggregate(httpTestkitTestCommons,
              httpTestkitCore, httpTestkitClient, httpTestkitServer, httpTestkitSpecs2, httpTestkit, httpTestkitMarshallerJackson,
              httpTestkitContractTests, httpTestkitContractTestsCustomMarshaller, httpTestkitContractTestsNoCustomMarshaller)
