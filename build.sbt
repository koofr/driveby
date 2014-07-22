name := "driveby"

organization := "net.koofr"

version := "0.1.2"

scalaVersion := "2.10.4"

resolvers += "Spray" at "http://repo.spray.io/"

libraryDependencies ++= Seq(
  "io.spray" % "spray-client" % "1.3.1",
  "io.spray" %% "spray-json" % "1.2.6",
  "com.typesafe.akka" % "akka-actor_2.10" % "2.3.3",
  "com.typesafe.akka" % "akka-testkit_2.10" % "2.3.3",
  "commons-codec" % "commons-codec" % "1.8",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.5",
  "org.slf4j" % "slf4j-api" % "1.7.2",
  "org.specs2" %% "specs2" % "2.2.3" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.2" % "test"
)

scalacOptions in Test ++= Seq("-Yrangepos")

parallelExecution in Test := false

testOptions in Test += Tests.Argument("sequential")

publishMavenStyle := true

publishArtifact in Test := false

publishTo <<= isSnapshot { isSnapshot =>
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot) Some("snapshots" at nexus + "content/repositories/snapshots")
  else            Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := {
    <inceptionYear>2014</inceptionYear>
    <url>http://github.com/koofr/driveby</url>
    <licenses>
      <license>
        <name>MIT License</name>
        <url>http://www.opensource.org/licenses/mit-license.php</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:koofr/driveby.git</url>
      <connection>scm:git:git@github.com:koofr/driveby</connection>
    </scm>
    <developers>
      <developer>
        <id>edofic</id>
        <name>Andraz Bajt</name>
        <url>https://github.com/edofic</url>
      </developer>
    </developers>
  }
