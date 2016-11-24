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
  resolvers := wixArtifactory,

  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8"),
  scalacOptions ++= Seq(
    "-language:reflectiveCalls",
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xfatal-warnings"
  )
)

val fwVersion = "2.1018.0-SNAPSHOT"

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



lazy val httpServerTestkit =
  Project(
    id = "http-server-testkit",
    base = file( "http-server-testkit" ),
    //    .crossType( CrossType.Pure )
    //    .in( file( "http-server-testkit" ) )
    settings = Seq(
      name := "http-server-testkit",
      libraryDependencies ++= akkaHttp ++ specs2 ++ wixFWDependenciesFor(fwVersion),
      description :=
        "Some crap i need to describe the library"
    ) ++ baseSettings
  )


lazy val root =
  Project(
    id = "root",
    base = file( "." ),
    settings = baseSettings ++ noPublish
  ).aggregate( httpServerTestkit )
