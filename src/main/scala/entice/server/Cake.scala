/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol._
import entice.protocol.utils._
import entice.protocol.utils.MessageBus._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.pattern.gracefulStop
import akka.pattern.pipe
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

import java.net.InetSocketAddress


/**
 * Delicious cake slice ;) - Bottom of the cake
 * Implements the most basic "core" functionality.
 */
trait CoreSlice {

    // the actor system and server actor must always be given
    lazy val actorSystem = ActorSystem("default")
    lazy val serverActor = actorSystem.actorOf(Props(new Actor { def receive = { case _ => } })) // do nothing actor

    final lazy val messageBus: MessageBus = new MessageBus()
}


/**
 * Delicious cake slice ;)
 * Holds the API components, needs to be completed by adding the handlers
 */
trait ApiSlice extends CoreSlice {

    // standard actors
    final lazy val reactor = actorSystem.actorOf(Props(classOf[ReactorActor], messageBus), s"reactor-${java.util.UUID.randomUUID().toString}")
}


object NetSlice {
    case class BindSuccess(addr: InetSocketAddress) extends Message
    case class BindFail extends Message
}


/**
 * Delicious cake slice ;)
 * Encapsulates network functionality.
 */
trait NetSlice extends CoreSlice with ApiSlice {

    lazy val localAddress = new InetSocketAddress(0)
    // set when the acceptor suceeds in binding
    var actualAddress: Option[InetSocketAddress] = None 
    
    // network stuff
    lazy val acceptor = SessionAcceptorFactory(actorSystem, localAddress)
}


object ActorSlice {
    case object Start
    case object Stop

    // internally
    case class SendTo(otherServer: ActorRef, msg: Message)
}


/**
 * Delicious cake slice ;) - Top of the cake
 * Add this layer to make a server actor out of your cake.
 */
trait ActorSlice extends Actor with CoreSlice with ApiSlice with NetSlice {

    import ReactorActor._
    import MetaReactorActor._
    import NetSlice._
    import ActorSlice._

    // by using this slice the server will be actor based,
    // and the serverActor obviously is this slice
    override lazy val serverActor = self

    // subscribe for acceptor events
    reactor ! Subscribe(self, classOf[BindSuccess])
    reactor ! Subscribe(self, classOf[BindFail])

    def receive = {

        // acceptor events
        case MessageEvent(a, BindSuccess(addr)) => 
            actualAddress = Some(addr)
        case MessageEvent(a, BindFail()) => 
            context stop self

        // external events
        case Start => 
            acceptor ! AddReactor(reactor)
        case Stop => 
            actorSystem.stop(acceptor)
            sender ! true

        // internal events
        case SendTo(srv, msg) =>
            srv ! msg
    }
}


/**
 * Delicious Cherry ontop of the cake.
 * Starts a server actor automatically.
 */
trait AutoStart extends Actor {

    import ActorSlice._

    // start the server automatically
    override def preStart {
        self ! Start
    }
}