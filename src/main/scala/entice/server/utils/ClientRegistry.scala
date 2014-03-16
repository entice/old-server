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
    var entriesNet:      DualMutableMap[ActorRef, Client] = DualMutableMap()
    var entriesEntities: DualMutableMap[Entity, Client]   = DualMutableMap()

    def add(client: Client) { 
        entriesNet += (client.session -> client) 
        client.entity map { rich =>
            entriesEntities += (rich.entity -> client)
        }
    }

    def update(client: Client) {
        // update the net<->client dualmap, regardless of what is in it
        entriesNet += (client.session -> client)
        // update the entity<->client dualmap, depending on whether we have an entity or not
        client.entity match {
            case Some(rich) => entriesEntities += (rich.entity -> client)
            case _          => entriesEntities removeRight (client)
        }
    }

    def remove(session: ActorRef) { 
        entriesNet >> (session) map remove
    }

    def remove(entity: Entity) { 
        entriesEntities >> (entity) map remove
    }

    def remove(client: Client) { 
        entriesNet      removeRight (client)
        entriesEntities removeRight (client)
    }

    def get(session: ActorRef) = entriesNet >> (session)

    def get(entity: Entity) = entriesEntities >> (entity)

    def getAll = entriesNet.valuesRight
}