/**
 * For copyright information see the LICENSE document.
 */

package entice.server.game

import entice.server.utils._
import entice.protocol._

import akka.actor.ActorRef


object Client {

    def apply(
        uuid: UUID,
        session: ActorRef, 
        entityMan: EntityManager): Client = {

        val player = Client(uuid, session, Entity(uuid))

        // register the player and its entity
        entityMan + (player.entity, Name("Standard"), Position(), Movement())

        player
    }
}


/**
 * CLient data storage
 * TODO: add DAO stuff
 */
case class Client(
    uuid: UUID, 
    session: ActorRef,
    entity: Entity,
    var state: PlayState = PrePlaying)


trait PlayState
case object PrePlaying      extends PlayState
case object Playing         extends PlayState
case object Dispatching     extends PlayState
case object Disconnecting   extends PlayState