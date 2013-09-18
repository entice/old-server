/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import akka.actor.ActorSystem
import akka.actor.{ Extension, ExtensionId, ExtensionIdProvider, ExtendedActorSystem }
import scala.pickling._
import json._

import scala.io._


object Config 
    extends ExtensionId[Config]
    with ExtensionIdProvider {

    val default = Config("127.0.0.1", 8112, 9112)

    def fromFile(file: String): Option[Config] = {
        try {
            val source = Source.fromFile(file).mkString.trim
            val result = toJSONPickle(source).unpickle[Config]
            return Some(result)
        } catch {
            return None
        }
    }

    override def lookup = Config
    override def createExtension(system: ExtendedActorSystem) = fromFile ("config.json") getOrElse (default)
    override def get(system: ActorSystem): Config = super.get(system)
}


/**
 * Encapsulates the complete config file.
 * (I'd rather not depend on typesafe's config stuff...)
 */
case class Config(
    host: String, 
    loginPort: Int, 
    gamePort: Int)
    extends Extension