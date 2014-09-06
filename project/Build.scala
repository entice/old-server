/**
 * For copyright information see the LICENSE document.
 */

import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._


object ProjectBuild extends Build {

  val project  = "0.1.0"
  val scala    = "2.11.2"
  val akka     = "2.3.3"


  val prjSettings = Project.defaultSettings ++ Seq(
    version      := project,
    scalaVersion := scala,

    scalacOptions ++= Seq(
      "-unchecked",
      "-feature",
      "-deprecation",
      "-Xlint",
      "-encoding", "UTF-8"
    ),

    resolvers ++= Seq(
      "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "releases"  at "https://oss.sonatype.org/content/repositories/releases",
      "typesafe"  at "https://repo.typesafe.com/typesafe/releases/"
    ),

    libraryDependencies ++= Seq(
      "org.scala-lang"             %  "scala-library"   % scala,
      "org.scala-lang"             %  "scala-reflect"   % scala,
      "org.codehaus.groovy"        %  "groovy-all"      % "2.3.6",
      "ch.qos.logback"             %  "logback-classic" % "1.1.2",
      "com.typesafe.akka"          %% "akka-actor"      % akka,
      "com.typesafe.akka"          %% "akka-slf4j"      % akka,
      "org.scala-lang"             %% "scala-pickling"  % "0.9.0-SNAPSHOT",
      "org.scalatest"              %% "scalatest"       % "2.2.1" % "test",
      "com.typesafe.akka"          %% "akka-testkit"    % akka    % "test"
    )
  )


  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = prjSettings ++ packageArchetype.java_application ++ Seq(
      name := "Entice Server",

      fork in run := true,
      connectInput in run := true,
      javaOptions in run ++= Seq(
        "-Dserver.port=9112",
        "-Dapp.env=DEV"
      ),

      fork in test := true,
      javaOptions in test ++= Seq(
        "-Dserver.port=9112",
        "-Dapp.env=TEST"
      )
    )
  ) .dependsOn(macros)
    .aggregate(macros)
    .dependsOn(protocol)
    .aggregate(protocol)


  lazy val macros = Project(
    id = "macros",
    base = file("macros"),
    settings = prjSettings
  )


  lazy val protocol = Project(
    id = "protocol",
    base = file("protocol"),
    settings = prjSettings
  )
}
