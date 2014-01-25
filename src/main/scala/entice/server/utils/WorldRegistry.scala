/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import entice.server._
import entice.server.world._
import entice.server.pathfinding._
import entice.protocol._
import akka.actor.{ ActorRef, Extension }


/**
 * Associates a session with a client object.
 */
class WorldRegistry(messageBus: MessageBus) extends Extension {

    private val defaultDir = Config.get.pmaps // TODO refactor me (?)
    private val defaultMap = "HeroesAscent"
    var worlds: Map[String, World] = 
        Map((defaultMap -> new World(
            defaultMap, 
            messageBus, 
            PathingMap.fromFile(defaultDir + Maps.withMapName(defaultMap).pmap).get)))

    def default = worlds(defaultMap)

    def get(map: String) = { 
        worlds.get(map) match {
            case Some(world) => world
            case None =>
                worlds = worlds + (map -> new World(
                    map, 
                    messageBus, 
                    PathingMap.fromFile(defaultDir + Maps.withMapName(defaultMap).pmap).get))
                worlds(map)
        }
    }

    def getAll() = worlds.values.toList
}