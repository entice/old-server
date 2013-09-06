/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol.utils._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props, PoisonPill }
import akka.pattern.gracefulStop
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

import java.net.InetSocketAddress


/**
 * Delicious cake slice ;) - Bottom of the cake
 * Implements the most basic "core" functionality.
 */
trait CoreSlice {
    // the actor system must always be given
    lazy val actorSystem = ActorSystem("default")

    lazy val messageBus = new MessageBus()
}


/**
 * Delicious cake slice ;)
 * Holds the API components, needs to be completed by adding the handlers
 */
trait ApiSlice extends CoreSlice {

    // standard actors
    final lazy val reactor = actorSystem.actorOf(Props(classOf[ReactorActor], messageBus), "reactor")
}


/**
 * Delicious cake slice ;)
 * Encapsulates network functionality.
 */
trait NetSlice extends CoreSlice with ApiSlice {

    lazy val localAddress = new InetSocketAddress(0)
    
    // network stuff
    final lazy val acceptor = actorSystem.actorOf(Props(classOf[SessionAcceptorActor], localAddress, reactor), "session-acceptor")    
}


object ActorSlice {
    case object Start
    case object Stop
}


/**
 * Delicious cake slice ;) - Top of the cake
 * Add this layer to make a server actor out of your cake.
 */
trait ActorSlice extends Actor with CoreSlice with ApiSlice with NetSlice {

    import ActorSlice._

    def receive = {
        case Start => acceptor
        case Stop =>
            try {
                val stopped: Future[Boolean] = gracefulStop(acceptor, Duration(5, SECONDS))
                Await.result(stopped, Duration(6, SECONDS))
            } catch {
                case e: akka.pattern.AskTimeoutException => 
            } finally {
                actorSystem.shutdown
            }

            sender ! true
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