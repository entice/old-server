name := "entice-server"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

resolvers ++= Seq(
  "Sonatype Releases"  at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.julienrf"      %% "play-json-variants"  % "0.2",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.0-SNAPSHOT",
  "com.typesafe.akka" %% "akka-actor"          % "2.3.4",
  "com.typesafe.akka" %% "akka-slf4j"          % "2.3.4",
  "com.typesafe.akka" %% "akka-testkit"        % "2.3.4" % "test"
)

javaOptions in Test += "-Dconfig.file=conf/test.conf"

parallelExecution in Test := false

lazy val root = project.in(file("."))
  .enablePlugins(PlayScala)
  .aggregate(macros)
  .dependsOn(macros)

lazy val macros = project
