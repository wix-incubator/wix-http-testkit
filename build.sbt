import sbt.Keys.resolvers
import depends._


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
  //  resolvers := wixArtifactory,

  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.12.0"),
  scalacOptions ++= Seq(
    "-language:reflectiveCalls",
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xfatal-warnings"
  )
)

//val fwVersion = "2.1019.0-SNAPSHOT"

lazy val noPublish = Seq( publish := {}, publishLocal := {}, publishArtifact := false )

lazy val baseSettings =
  publishSettings ++
    releaseSettings ++
    compileOptions ++
    //  sbtdoge.CrossPerProjectPlugin.projectSettings ++
    Seq(
      organization := "com.wix",
      homepage := Some( url( "https://github.com/wix-private/http-testkit" ) ),
      licenses := Seq( "Apache-2.0" -> url( "http://www.opensource.org/licenses/Apache-2.0" ) )
    )



lazy val httpTestkitTestCommons =
  Project(
    id = "http-testkit-test-commons",
    base = file( "http-testkit-test-commons" ),
    settings = Seq(
      name := "http-testkit-test-commons",
      description := "Some crap i need to describe the library"
    ) ++ baseSettings
  )

lazy val httpTestkitCore =
  Project(
    id = "http-testkit-core",
    base = file( "http-testkit-core" ),
    settings = Seq(
      name := "http-testkit-core",
      libraryDependencies ++= akkaHttp ++ jackson ++ joda ++ specs2.map(_ % "test") :+ scalaXml,
      description :=
        "Some crap i need to describe the library"
    ) ++ baseSettings
  ).dependsOn(httpTestkitTestCommons)

lazy val httpTestkitClient =
  Project(
    id = "http-testkit-client",
    base = file( "http-testkit-client" ),
    settings = Seq(
      name := "http-testkit-client",
      libraryDependencies ++= specs2.map(_ % "test") ,
      description :=
        "Some crap i need to describe the library"
    ) ++ baseSettings
  ).dependsOn(httpTestkitCore, httpTestkitSpecs2)

lazy val httpTestkitServer =
  Project(
    id = "http-testkit-server",
    base = file( "http-testkit-server" ),
    settings = Seq(
      name := "http-testkit-server",
      description :=
        "Some crap i need to describe the library"
    ) ++ baseSettings
  ).dependsOn(httpTestkitCore)

lazy val httpTestkitSpecs2 =
  Project(
    id = "http-testkit-specs2",
    base = file( "http-testkit-specs2" ),
    settings = Seq(
      name := "http-testkit-specs2",
      libraryDependencies ++= specs2,
      description :=
        "Some crap i need to describe the library"
    ) ++ baseSettings
  ).dependsOn(httpTestkitCore, httpTestkitTestCommons)

lazy val wixHttpTestkit =
  Project(
    id = "wix-http-testkit",
    base = file( "wix-http-testkit" ),
    settings = Seq(
      name := "wix-http-testkit",
      description :=
        "Some crap i need to describe the library"
    ) ++ baseSettings
  ).dependsOn(httpTestkitClient, httpTestkitServer)

lazy val httpTestkitContractTests =
  Project(
    id = "http-testkit-contract-tests",
    base = file( "http-testkit-contract-tests" ),
    settings = Seq(
      name := "http-testkit-contract-tests",
      libraryDependencies ++= specs2.map(_ % "test") ,
      description :=
        "Some crap i need to describe the library"
    ) ++ baseSettings
  ).dependsOn(wixHttpTestkit, httpTestkitTestCommons, httpTestkitSpecs2)

lazy val httpTestkitExamples =
  Project(
    id = "examples",
    base = file( "examples" ),
    settings = Seq(
      name := "examples",
      libraryDependencies ++= specs2.map(_ % "test") ,
      description :=
        "Some crap i need to describe the library"
    ) ++ baseSettings
  ).dependsOn(wixHttpTestkit, httpTestkitSpecs2)


lazy val root =
  Project(
    id = "root",
    base = file( "." ),
    settings = baseSettings ++ noPublish
  ).aggregate(httpTestkitTestCommons,
    httpTestkitCore, httpTestkitClient, httpTestkitServer, httpTestkitSpecs2, wixHttpTestkit,
    httpTestkitContractTests)
