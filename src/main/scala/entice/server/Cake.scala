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
    lazy val serverActor = actorSystem.actorOf(Props.empty) // do nothing actor

    final lazy val messageBus: MessageBus = new MessageBus()
}


/**
 * Delicious cake slice ;)
 * Holds the API components, needs to be completed by adding the handlers
 */
trait ApiSlice extends CoreSlice {
    // fill this list in your own environment with actor props of your API actors
    def props: List[Props]

    def createApi {
        props foreach { actorSystem.actorOf(_) }
    }
}


object ServerActorSlice {
    case object Start
    case object Stop

    // internally
    case class SendTo(otherServer: ActorRef, msg: Message)
}


/**
 * Delicious cake slice ;) - Top of the cake
 * Add this layer to make a server actor out of your cake.
 */
trait ServerActorSlice extends Actor with CoreSlice with ApiSlice with NetSlice {

    import Grid._
    import ServerActorSlice._

    override lazy val actorSystem = context
    override lazy val serverActor = self

    override def preStart {
        createApi        
    }

    def receive = {
        // external events
        case Start          => IO(Grid) ! Bind()
        case Stop           => context stop self

        // acceptor events
        case BindSuccess    => // do nothing atm 
        case BindFailure    => context stop self
        case NewSession     => // do nothing atm... (context watch session)

        // session events
        case NewMessage(m)  => 
            val session = sender;
            messageBus.publish(MessageEvent(session, m))

        // internal events
        case SendTo(srv, msg) =>
            srv ! msg
    }
}


/**
 * Delicious Cherry ontop of the cake.
 * Starts a server actor automatically.
 */
trait AutoStart extends Actor with ServerActorSlice {

    import ServerActorSlice._

    override def preStart {
        super.preStart
        self ! Start
    }
}