name := "Entice Server"

version := "0.0.1"

scalaVersion := "2.10.2"

resolvers ++= Seq(
    "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
    "releases"  at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.2.0",
    "com.typesafe.akka" %% "akka-testkit" % "2.2.0",
    "org.scalatest" %% "scalatest" % "1.9.1" % "test" 
)
