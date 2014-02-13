/**
 * For copyright information see the LICENSE document.
 */

import sbt._
import sbt.Keys._


object ProjectBuild extends Build {
    
    lazy val root = Project(
        id = "server", 
        base = file(".")
    ) dependsOn(protocol)

    lazy val protocol = RootProject(uri("https://github.com/entice/protocol.git#milestone4"))
}