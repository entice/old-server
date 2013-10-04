/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.controllers._
import entice.server.world._
import entice.server.systems._
import entice.server.utils._
import entice.protocol._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.pattern.gracefulStop
import akka.pattern.pipe
import akka.io.IO
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
}


/**
 * Delicious cake slice ;)
 * Holds the API components, needs to be completed by adding the handlers
 */
trait ControllerSlice extends CoreSlice {
    // fill this list in your own environment with actor props of your API actors
    def props: List[Props] =
        // handlers
        Props(classOf[Login]) ::
        Props(classOf[Play]) ::
        Props(classOf[CharCreate]) ::
        Props(classOf[WorldDiff]) ::
        Props(classOf[Command]) ::
        Props(classOf[Disconnect]) ::
        // systems
        Props(classOf[ChatSystem]) :: Props(classOf[PreChat]) :: 
        Props(classOf[MovementSystem]) :: Props(classOf[PreMovement]) ::
        Props(classOf[AnimationSystem]) ::
        Props(classOf[SchedulingSystem]) ::
        Nil
}


/**
 * Delicious cake slice ;)
 * Add this layer to get a server tick on the message bus that your handlers
 * can listen for and react to.
 */
trait TickingSlice extends CoreSlice {

    import actorSystem.dispatcher

    lazy val interval = 50

    // schedule tick a fixed rate
    actorSystem.scheduler.schedule(
        Duration.Zero,                       // initial delay duration
        Duration(interval, MILLISECONDS))(   // delay for each invokation
            MessageBusExtension(actorSystem).publish(MessageEvent(serverActor, Tick()))
        )
}


/**
 * Delicious cake slice ;) - Top of the cake
 * Add this layer to make a server actor out of your cake.
 */
class ServerActorSlice 
    extends Actor 
    with CoreSlice with ControllerSlice with TickingSlice 
    with Configurable 
    with Subscriber {

    import Net._

    override lazy val actorSystem = context.system
    override lazy val serverActor = self
    val subscriptions = Nil

    override def preStart {
        import context.system
        // create the handlers
        props foreach { context.actorOf(_) }
        // and the listening socket
        IO(Net) ! Start(new InetSocketAddress(config.port))
    }

    def receive = {
        // acceptor events
        case BindSuccess    => // do nothing atm 
        case BindFailure    => context stop self
        case n: NewSession  => MessageEvent(self, n)
        case l: LostSession => MessageEvent(self, l)

        // session events
        case NewMessage(m)  => 
            val session = sender;
            messageBus.publish(MessageEvent(session, m))
    }
}