
import sbt._
import sbt.Keys._
import scalabuff.ScalaBuffPlugin._

object ProjectBuild extends Build {
    
    lazy val root = Project(
        id = "main", 
        base = file("."), 
        settings = Defaults.defaultSettings ++ scalabuffSettings
    ).configs(ScalaBuff)
}