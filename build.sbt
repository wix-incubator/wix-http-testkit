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
  scalaVersion := "2.12.5",
  crossScalaVersions := Seq("2.11.12", "2.12.5"/*, "2.13-M2"*/),
  scalacOptions ++= Seq(
    "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
    "-encoding", "utf-8",                // Specify character encoding used by source files.
    "-explaintypes",                     // Explain type errors in more detail.
    "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
    "-language:implicitConversions",     // Allow definition of implicit functions called views
    "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
    "-Xfuture",                          // Turn on future language features.
    "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
    "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
//    "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
    "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
    "-Xlint:option-implicit",            // Option.apply used implicit view.
    "-Xlint:package-object-classes",     // Class or object defined in package object.
    "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
    "-Xlint:unsound-match",              // Pattern match may not be typesafe.
    "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    "-Ypartial-unification",             // Enable partial unification in type constructor inference
    "-Ywarn-dead-code",                  // Warn when dead code is identified.
//    "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
    "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit"               // Warn when nullary methods return Unit.
//    "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
//    "-Ywarn-unused:locals",              // Warn if a local definition is unused.
//    "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
//    "-Ywarn-unused:privates"            // Warn if a private member is unused.
  )
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
    .settings(Seq(
      name := "http-testkit-core",
      libraryDependencies ++= joda ++ specs2Test(scalaVersion.value) ++ akkaHttp(scalaVersion.value) :+ scalaXml :+ reflections :+ jsr305 :+ slf4jApi,
      description := "Commonly used util code also client and server interfaces"
    ) ++ baseSettings)
    .dependsOn(httpTestkitTestCommons % Test)

lazy val httpTestkitClient =
  (project in file("http-testkit-client"))
    .settings(Seq(
      name := "http-testkit-client",
      libraryDependencies ++= specs2(scalaVersion.value) ,
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
      libraryDependencies ++= specs2(scalaVersion.value),
      description := "Specs2 Matcher suites - Request and Response."
    ) ++ baseSettings)
    .dependsOn(httpTestkitCore, httpTestkitTestCommons % Test, httpTestkitMarshallerJackson % Test)

lazy val httpTestkitScalaTest =
  (project in file("http-testkit-scala-test"))
    .settings(Seq(
      name := "http-testkit-scalatest",
      libraryDependencies ++= Seq(scalaTest) :+ scalaMock,
      description := "Scala Test Matcher suites - Request and Response."
    ) ++ baseSettings)
    .dependsOn(httpTestkitCore, httpTestkitTestCommons % Test, httpTestkitMarshallerJackson % Test)

lazy val httpTestkitMarshallerJackson =
  (project in file("http-testkit-marshaller-jackson"))
    .settings(Seq(
      name := "http-testkit-marshaller-jackson",
      libraryDependencies ++= jackson(scalaVersion.value) ++ specs2(scalaVersion.value),
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
    .settings(Seq(
      name := "http-testkit-contract-tests",
      libraryDependencies ++= specs2Test(scalaVersion.value),
      description := "Contract tests for both client and server"
    ) ++ baseSettings ++ noPublish)
    .dependsOn(httpTestkit, httpTestkitMarshallerJackson, httpTestkitTestCommons % Test)

lazy val httpTestkitContractTestsCustomMarshaller =
  (project in file("contract-tests/marshaller-contract-tests/http-testkit-contract-tests-custom-marshaller"))
    .settings(Seq(
      name := "http-testkit-contract-tests-custom-marshaller",
      libraryDependencies ++= specs2Test(scalaVersion.value),
      description := "Contract tests for marshaller: cover cases which custom marshaller exists on classpath"
    ) ++ baseSettings ++ noPublish)
    .dependsOn(httpTestkit, httpTestkitTestCommons % Test)

lazy val httpTestkitContractTestsNoCustomMarshaller =
  (project in file("contract-tests/marshaller-contract-tests/http-testkit-contract-tests-no-custom-marshaller"))
    .settings(Seq(
      name := "http-testkit-contract-tests-no-custom-marshaller",
      libraryDependencies ++= specs2Test(scalaVersion.value),
      description := "Contract tests for marshaller: cover cases which no custom marshaller exists on classpath"
    ) ++ baseSettings ++ noPublish)
    .dependsOn(httpTestkit, httpTestkitTestCommons % Test)

lazy val httpTestkitContractTestsDualMarshallers =
  (project in file("contract-tests/marshaller-contract-tests/http-testkit-contract-tests-dual-marshallers"))
    .settings(Seq(
      name := "http-testkit-contract-tests-dual-marshallers",
      libraryDependencies ++= specs2Test(scalaVersion.value),
      description := "Contract tests for marshaller: cover cases which custom marshaller and default marshaller exists on classpath"
    ) ++ baseSettings ++ noPublish)
    .dependsOn(httpTestkit, httpTestkitMarshallerJackson, httpTestkitTestCommons % Test)

lazy val httpTestkitContractTestsMalformedMarshaller =
  (project in file("contract-tests/marshaller-contract-tests/http-testkit-contract-tests-malformed-marshaller"))
    .settings(Seq(
      name := "http-testkit-contract-tests-malformed-marshaller",
      libraryDependencies ++= specs2Test(scalaVersion.value),
      description := "Contract tests for marshaller"
    ) ++ baseSettings ++ noPublish)
    .dependsOn(httpTestkitCore)

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
               contractTests)
