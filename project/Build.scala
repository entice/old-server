/**
 * For copyright information see the LICENSE document.
 */

import sbt._
import sbt.Keys._


object ProjectBuild extends Build {
    
    lazy val root = Project(
        id = "server", 
        base = file(".")
    ) dependsOn(protocol, jsonMacros)

    lazy val protocol = RootProject(uri("https://github.com/entice/protocol.git#milestone1"))
    lazy val jsonMacros = RootProject(uri("https://github.com/ephe-meral/akmacros-json.git#fix-play-2.2-SNAPSHOT"))
}