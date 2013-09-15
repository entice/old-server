/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol._
import entice.protocol.utils._
import entice.protocol.utils.MessageBus._

import akka.actor.{ Actor, ActorRef, ActorLogging, ActorSystem, Props }

import java.net.InetSocketAddress


object ReactorActor {
    case class Subscribe(me: ActorRef, forMsg: Class[_ <: Message])
    case class Publish(sender: Sender, msg: Message)
}


/**
 * Convenience helper-trait... can we make the subscription process even more convenient?
 */
trait Subscriber {
    this: Actor =>

    import ReactorActor._

    def reactor: ActorRef
    def subscriptions: List[Class[_ <: Message]]

    def register {
        subscriptions map { reactor ! Subscribe(self, _) }
    }
}


/**
 * Encapsulates a pub/sub message bus.
 */
class ReactorActor(messageBus: MessageBus) extends Actor with ActorLogging {

    import ReactorActor._


    def receive = {
        case Publish(sender, message) =>
            log.debug(s"Pub: ${sender.toString} -> ${message.`type`}")
            messageBus.publish(MessageEvent(sender, message))
        case Subscribe(actor, message) =>
            log.debug(s"Sub: ${actor.toString} for ${message.getSimpleName}")
            messageBus.subscribe(actor, message)
    }
}


object MetaReactorActor {
    case class AddReactor(reactor: ActorRef)
}


/**
 * Encapsulates several reactors. Publishes messages on all of them.
 */
class MetaReactorActor extends Actor with ActorLogging {

    import MetaReactorActor._
    import ReactorActor._

    var reactors: List[ActorRef] = Nil


    def receive = {
        case AddReactor(reactor) =>
            reactors = reactor :: reactors
        case p: Publish =>
            reactors map { _ ! p }
    }
}