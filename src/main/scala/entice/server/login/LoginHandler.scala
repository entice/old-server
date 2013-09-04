/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.server._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

import entice.protocol._
import entice.protocol.utils.MessageBus.MessageEvent


class LoginFactory(actorSystem: ActorSystem, reactor: ActorRef) {

    import ReactorActor._

    // used to wire this class automatically and subscribe it with the reactor
    case class createAndSubscribe {

        lazy val loginService = actorSystem.actorOf(Props[LoginHandler])

        reactor ! Subscribe(loginService, classOf[LoginRequest])
    }
}


class LoginHandler extends Actor {

    def receive = {

        case MessageEvent(session, LoginRequest("test", "test")) => 
            session ! LoginResponse("Wrong login credentials.")

        case MessageEvent(session, login: LoginRequest) => 
            session ! LoginResponse()
    }
}