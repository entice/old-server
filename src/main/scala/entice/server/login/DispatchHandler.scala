/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.server._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

import entice.protocol._
import entice.protocol.utils.MessageBus.MessageEvent


class DispatchFactory(actorSystem: ActorSystem, reactor: ActorRef) {

    import ReactorActor._

    // used to wire this class automatically and subscribe it with the reactor
    case class createAndSubscribe {

        lazy val dispatchService = actorSystem.actorOf(Props[DispatchHandler])

        reactor ! Subscribe(dispatchService, classOf[DispatchRequest])
    }
}


class DispatchHandler extends Actor {

    def receive = {

        case MessageEvent(session, DispatchRequest()) => 
            session ! DispatchResponse("localhost", 9112, 313373)
    }
}