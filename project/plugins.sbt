logLevel := Level.Warn

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC12")
//addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

credentials ++= (
  for {
    username <- Option( System.getenv().get( "SONATYPE_USERNAME" ) )
    password <- Option( System.getenv().get( "SONATYPE_PASSWORD" ) )
  } yield Credentials( "Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password )
  ).toSeq
