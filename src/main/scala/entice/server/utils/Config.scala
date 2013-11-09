/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import akka.actor.Extension
import scala.pickling._, json._
import scala.io._


/**
 * Singleton.
 * This is not connected to the actor-framework in any way.
 */
object Config {
    def get = fromFile(defaultFile) getOrElse defaultData

    val defaultFile = "config/config.json"
    val defaultData = Config(
        "127.0.0.1", 
        8112, 
        "scripts/commands/",
        "maps/",
        30, 
        250)

    private def fromFile(file: String): Option[Config] = {
        try {
            val source = Source.fromFile(file).mkString.trim
            val result = toJSONPickle(source).unpickle[Config]
            return Some(result)
        } 
        return None
    }
}


/**
 * Encapsulates the complete config file.
 * (I'd rather not depend on typesafe's config stuff...)
 */
case class Config(
    host: String, 
    port: Int,
    commands: String,
    pmaps: String,
    minTick: Int,
    maxTick: Int) extends Extension