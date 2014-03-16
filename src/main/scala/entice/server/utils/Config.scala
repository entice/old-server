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
        "entice",
        30,
        50, 
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
 * 
 * @param   host        The hostname or IP of this machine
 * @param   port        The TCP port this should run on
 * @param   commands    The directory of the IG command scripts
 * @param   pmaps       The directory of the pathing maps we're using
 * @param   tick        The event interval that invokes the general server systems
 * @param   minUpdate   The minimum event interval that invokes a game-state push to client
 * @param   minUpdate   The maximum event interval that invokes a game-state push to client
 */
case class Config(
    host: String, 
    port: Int,
    commands: String,
    pmaps: String,
    database: String,
    tick: Int,
    minUpdate: Int,
    maxUpdate: Int) extends Extension