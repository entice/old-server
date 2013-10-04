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
class WorldRegistry extends Extension {
    var world = new World

    def add(entry: Client) { /*TODO*/ }
    def get(entry: Client) = world
    def getAll() = List(world)
}