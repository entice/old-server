/**
 * For copyright information see the LICENSE document.
 */

package entice.server.game

import entice.server._
import entice.server.utils._
import entice.server.game.entitysystems._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

import java.net.InetSocketAddress
import scala.concurrent.duration._


/**
 * Delicious cake slice ;) - Replaces default API slice
 * Implements the whole gameplay functionality
 */
trait GameApiSlice extends CoreSlice with ApiSlice {

    lazy val clientRegistry = new Registry[Client]
    lazy val entityManager = new EntityManager

    // handler actors
    val props =
        Props(classOf[ConnectionHandler],   messageBus, clientRegistry, entityManager) ::
        Props(classOf[MoveHandler],         messageBus, clientRegistry, entityManager) ::

    // systems
        Props(classOf[WorldDiffSystem],     messageBus, clientRegistry, entityManager) ::
        Nil
}


/**
 * Delicious cake slice ;)
 * Add this layer to get a server tick on the message bus that your handlers.
 * can listen for and react to.
 */
trait TickingSlice extends CoreSlice with ApiSlice {

    import actorSystem.dispatcher

    lazy val interval = 50

    // schedule tick a fixed rate
    actorSystem.scheduler.schedule(
        Duration.Zero,                      // initial delay duration
        Duration(interval, MILLISECONDS),   // delay for each invokation
        new Runnable {
            def run = {
                messageBus.publish(serverActor, Tick())
            }
        })
}


/**
 * Understands the LS2GS communication protocol.
 */
case class GameServer(system: ActorSystem) extends Actor 
    with CoreSlice 
    with GameApiSlice
    with TickingSlice
    with ServerActorSlice {

    val port = Config(system).loginPort
    override lazy val actorSystem = system
    override lazy val localAddress = new InetSocketAddress(port)


    override def receive = super.receive orElse {
        case msg: LS2GS =>
            val loginSrv = sender
            messageBus.publish(loginSrv, msg)
    }
}