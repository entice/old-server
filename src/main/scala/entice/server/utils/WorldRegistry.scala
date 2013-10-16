/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import entice.server._
import entice.server.world._
import akka.actor.{ ActorRef, Extension }


/**
 * Associates a session with a client object.
 */
class WorldRegistry(messageBus: MessageBus) extends Extension {
    var worlds: Map[String, World] = Map(("TeamArenas" -> new World("TeamArenas", messageBus)))

    def default = worlds("TeamArenas")

    def get(map: String) = { 
        worlds.get(map) match {
            case Some(world) => world
            case None =>
                worlds = worlds + (map -> new World(map, messageBus))
                worlds(map)
        }
    }

    def getAll() = worlds.values.toList
}