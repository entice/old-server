/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import entice.server._
import entice.protocol._
import akka.actor.{ ActorRef, Extension }


/**
 * Associates a session with a client object.
 *
 * TODO: Cleanup, refactor.
 */
class ClientRegistry extends Extension {
    var entriesNet: Map[ActorRef, Client] = Map()
    var entriesEntities: Map[Entity, Client] = Map()

    def add(entry: Client) { 
        entriesNet = entriesNet + (entry.session -> entry) 
        entry.entity map { rich =>
            entriesEntities = entriesEntities + (rich.entity -> entry)
        }
    }

    def remove(session: ActorRef) { 
        entriesNet.get(session) map { client =>
            entriesNet = entriesNet - session
            client.entity map { rich =>
                entriesEntities = entriesEntities - rich.entity
            }
        }
    }

    def remove(entity: Entity) { 
        entriesEntities.get(entity) map { client =>
            entriesNet = entriesNet - client.session
            entriesEntities = entriesEntities - entity
        }
    }

    def remove(entry: Client) { 
        remove(entry.session)
        entry.entity map { rich => remove(rich.entity) }
    }

    def get(session: ActorRef) = entriesNet.get(session)

    def get(entity: Entity) = entriesEntities.get(entity)

    def getAll = entriesNet.values
}