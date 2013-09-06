/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.server._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

import entice.protocol._
import entice.protocol.utils.MessageBus.MessageEvent


class DispatchHandler(val reactor: ActorRef) extends Actor with Subscriber {

    val subscriptions =
        classOf[DispatchRequest] ::
        Nil

    override def preStart {
        register
    }

    def receive = {

        case MessageEvent(session, DispatchRequest()) => 
            session ! DispatchResponse("localhost", 9112, 313373)
    }
}