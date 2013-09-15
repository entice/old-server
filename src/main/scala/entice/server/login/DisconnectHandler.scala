/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.server._
import entice.server.utils._
import entice.protocol._
import entice.protocol.utils.MessageBus._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }


class DisconnectHandler(
    val reactor: ActorRef,
    val clients: Registry[Client]) extends Actor with Subscriber {


    val subscriptions =
        classOf[SessionDisconnect] ::
        Nil


    override def preStart {
        register
    }


    def receive = {
        case MessageEvent(Sender(id: UUID, _), SessionDisconnect()) =>
            clients.remove(id)
    }
}