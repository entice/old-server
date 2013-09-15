/**
 * For copyright information see the LICENSE document.
 */

package entice.server.game

import entice.server._
import entice.server.utils._
import entice.protocol._
import entice.protocol.utils.MessageBus._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }


class MoveHandler(
    val reactor: ActorRef,
    val players: Registry[Player],
    val entityMan: EntityManager) extends Actor with Subscriber {

    import ReactorActor._
    

    val subscriptions =
        classOf[MoveRequest] ::
        Nil


    override def preStart {
        register
    }


    def receive = {

        case MessageEvent(Sender(id, sess), m: MoveRequest) => 
            players.get(id) map { p: Player =>
                val pos = entityMan.getCompBy(p.entity, classOf[Position]).get
                val move = entityMan.getCompBy(p.entity, classOf[Movement]).get

                // is there a way to avoid manual copying?
                pos.pos = m.pos.pos
                move.dir = m.move.dir
                move.state = m.move.state

                // flush
                reactor ! Publish(Sender(actor = self), Flush())
            }
    }
}