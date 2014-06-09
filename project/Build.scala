/**
 * For copyright information see the LICENSE document.
 */

import sbt._
import sbt.Keys._


object ProjectBuild extends Build {


  val project  = "0.1.0"
  val scala    = "2.11.0"
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
      "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "releases"  at "http://oss.sonatype.org/content/repositories/releases",
      "typesafe"  at "http://repo.typesafe.com/typesafe/releases/"
    ),

    libraryDependencies ++= Seq(
      "org.scala-lang"         %  "scala-library" % scala,
      "org.scala-lang"         %  "scala-reflect" % scala,
      "com.typesafe.akka"      %% "akka-actor"    % akka,
      "com.typesafe.akka"      %% "akka-agent"    % akka,
      "org.scalatest"          %% "scalatest"     % "2.1.3" % "test",
      "com.typesafe.akka"      %% "akka-testkit"  % akka    % "test"
    )
  )
    

  lazy val root = Project(
    id = "root", 
    base = file("."),
    settings = prjSettings ++ Seq(
      name := "Entice Server"
    )
  ) 
    .dependsOn(macros)
    .dependsOn(protocol)
    .aggregate(protocol)


  lazy val macros = Project(
    id = "macros", 
    base = file("macros"),
    settings = prjSettings
  )


  lazy val protocol = RootProject(uri("https://github.com/entice/protocol.git#milestone5"))
}