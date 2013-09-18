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


class ConnectionHandler(
    val messageBus: MessageBus,
    val clients: Registry[Client],
    val entityMan: EntityManager,
    val serverActor: ActorRef) extends Actor with Subscriber {


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
        case MessageEvent(ls, AddPlayer(uuid, key)) =>
            if (! loginServer.isDefined) { loginServer = Some(ls) }
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

                session ! OnlyReportTo(serverActor)
                session ! PlaySuccess(player.entity, EntityView(entityMan.getAll))
                context watch session

                // enable the player
                player.state = Playing
            }

        // a session terminated
        case Terminated(_) =>
            val sess = sender
            players.get(id) map { p: Player =>
                p.state = Disconnecting
                entityMan.unregister(p.entity)                
            }
            players.remove(id)
    }
}