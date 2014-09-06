/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.configs

import entice.server._

import scala.pickling._, json._

import scala.io.Source
import scala.util.Try


trait JsonConfig extends Config { self: Logger =>

  lazy val configFile = "conf/config.json"
  lazy val config = parseConfig(configFile) getOrElse defaultConfig

  private def parseConfig(file: String): Option[Config] = {
    (for {
      src <- Try(Source.fromFile(file).mkString.trim)
      res <- Try(toJSONPickle(src).unpickle[Config])
    } yield { res }).toOption
  }
}
