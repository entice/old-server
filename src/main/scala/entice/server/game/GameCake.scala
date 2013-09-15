/**
 * For copyright information see the LICENSE document.
 */

package entice.server.game

import entice.server._
import entice.server.utils._
import entice.server.game.entitysystems._
import entice.protocol.utils.MessageBus._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

import java.net.InetSocketAddress
import scala.concurrent.duration._


trait GameApiSlice extends CoreSlice with ApiSlice {

    lazy val playerRegistry = new Registry[Player]
    lazy val entityManager = new EntityManager

    // handler actors
    val playHandler = actorSystem.actorOf(Props(classOf[PlayHandler], reactor, playerRegistry, entityManager), s"play-${java.util.UUID.randomUUID().toString}")
    val moveHandler = actorSystem.actorOf(Props(classOf[MoveHandler], reactor, playerRegistry, entityManager), s"move-${java.util.UUID.randomUUID().toString}")

    // systems
    val worldDiff = actorSystem.actorOf(Props(classOf[WorldDiffSystem], reactor, playerRegistry, entityManager), s"worldDiff-${java.util.UUID.randomUUID().toString}")
}


trait TickingSlice extends CoreSlice with ApiSlice {

    import ReactorActor._
    import actorSystem.dispatcher

    lazy val interval = 50

    // schedule tick a fixed rate
    actorSystem.scheduler.schedule(
        Duration.Zero, // initial delay duration
        Duration(interval, MILLISECONDS),
        reactor,
        Publish(Sender(actor = reactor), Tick()))
}


/**
 * Understands the LS2GS communication protocol.
 */
case class GameServer(system: ActorSystem, port: Int) extends Actor 
    with CoreSlice 
    with GameApiSlice
    with TickingSlice
    with NetSlice 
    with ActorSlice {

    import entice.server.ReactorActor._


    override lazy val actorSystem = system
    override lazy val localAddress = new InetSocketAddress(port)


    override def receive = super.receive orElse {
        case msg: LS2GS =>
            val loginSrv = sender
            reactor ! Publish(Sender(actor = loginSrv), msg)
    }
}