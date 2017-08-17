logLevel := Level.Warn

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC9")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

credentials ++= (
  for {
    username <- Option( System.getenv().get( "SONATYPE_USERNAME" ) )
    password <- Option( System.getenv().get( "SONATYPE_PASSWORD" ) )
  } yield Credentials( "Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password )
  ).toSeq

//resolvers += Resolver.url("bintray-sbt-plugins", url("https://dl.bintray.com/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)