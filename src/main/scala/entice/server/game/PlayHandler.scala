/**
 * For copyright information see the LICENSE document.
 */

package entice.server.game

import entice.server._
import entice.server.utils._
import entice.protocol._
import entice.protocol.utils.MessageBus._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

import scala.collection.mutable


class PlayHandler(
    val reactor: ActorRef,
    val players: Registry[Player],
    val entityMan: EntityManager) extends Actor with Subscriber {


    import SessionActor._

    var loginServer: Option[ActorRef] = None
    val waitingPlayers: mutable.Map[Long, UUID] = mutable.Map()

    val subscriptions =
        classOf[AddPlayer]   ::
        classOf[PlayRequest] ::
        Nil


    override def preStart {
        register
    }


    def receive = {

        // the LS wants us to accept a new player
        case MessageEvent(s: Sender, AddPlayer(uuid, key)) =>
            if (! loginServer.isDefined) { loginServer = Some(s.actor) }
            // TODO: check if we can actually take more players
            waitingPlayers += (key -> uuid)
            loginServer map { _ ! WaitingForPlayer(uuid) }

        // a player connects
        case MessageEvent(Sender(uuid, session), PlayRequest(key)) =>
            val uuid = waitingPlayers.get(key)
            uuid map { id: UUID =>
                val player = Player(id, session, entityMan)
                players += player
                waitingPlayers -= key
                session ! NewUUID(id)
                session ! Reactor(reactor)
                session ! PlaySuccess(player.entity, EntityView(entityMan.getAll))

                // enable the player
                player.state = Playing
            }
    }
}