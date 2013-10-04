/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import entice.server._
import akka.actor.{ ActorRef, Extension }


/**
 * Associates a session with a client object.
 */
class ClientRegistry extends Extension {
    var entries: Map[ActorRef, Client] = Map()

    def add(entry: Client) { entries = entries + (entry.session -> entry) }
    def remove(session: ActorRef) { entries = entries - session }
    def remove(entry: Client) { remove(entry.session) }
    def get(session: ActorRef) = entries.get(session)
    def getAll = entries.values
}