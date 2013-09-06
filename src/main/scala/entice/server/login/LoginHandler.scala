/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.server._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

import entice.protocol._
import entice.protocol.utils.MessageBus.MessageEvent


class LoginHandler(val reactor: ActorRef) extends Actor with Subscriber {

    val subscriptions =
        classOf[LoginRequest] ::
        Nil

    override def preStart {
        register
    }

    def receive = {

        case MessageEvent(session, LoginRequest("test", "test")) => 
            session ! LoginFail("Wrong login credentials.")

        case MessageEvent(session, login: LoginRequest) => 
            session ! LoginSuccess()
    }
}