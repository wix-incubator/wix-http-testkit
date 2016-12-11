import depends._

//lazy val javaRuntimeVersion = settingKey[ Double ]( "The JVM runtime version (e.g. 1.8)" )



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
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
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
    releaseSettings ++
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
      libraryDependencies ++= akkaHttp ++ jackson ++ joda ++ specs2.map(_ % Test) :+ scalaXml,
      description := "Commonly used util code also client and server interfaces"
    ) ++ baseSettings
  ).dependsOn(httpTestkitTestCommons)

lazy val httpTestkitClient =
  Project(
    id = "http-testkit-client",
    base = file( "http-testkit-client" ),
    settings = Seq(
      name := "http-testkit-client",
      libraryDependencies ++= specs2.map(_ % Test) ,
      description := "All code related to REST client, blocking and non-blocking"
    ) ++ baseSettings
  ).dependsOn(httpTestkitCore, httpTestkitSpecs2)

lazy val httpTestkitServer =
  Project(
    id = "http-testkit-server",
    base = file( "http-testkit-server" ),
    settings = Seq(
      name := "http-testkit-server",
      description := "Server implementations - stub and mock"
    ) ++ baseSettings ++ packAutoSettings
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
  ).dependsOn(httpTestkitCore, httpTestkitTestCommons)

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
  ).dependsOn(httpTestkit, httpTestkitTestCommons)

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
              httpTestkitCore, httpTestkitClient, httpTestkitServer, httpTestkitSpecs2, httpTestkit,
              httpTestkitContractTests)