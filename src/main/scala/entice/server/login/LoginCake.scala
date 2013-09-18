/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.server._
import entice.server.utils._
import entice.server.Config._
import entice.protocol.utils.MessageBus._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

import java.net.InetSocketAddress


/**
 * Delicious cake slice ;) - Replaces default API slice
 * Implements the whole login-server functionality.
 */
trait LoginApiSlice extends CoreSlice with ApiSlice {

    lazy val clientRegistry = new Registry[Client]

    // handler actors
    val props =
        Props(classOf[LoginHandler],        messageBus, clientRegistry) ::
        Props(classOf[DispatchHandler],     messageBus, clientRegistry, serverActor) ::
        Props(classOf[DisconnectHandler],   messageBus, clientRegistry) ::
        Nil
}


/**
 * Understands the LS2GS communication protocol.
 */
case class LoginServer(system: ActorSystem) extends Actor 
    with CoreSlice 
    with LoginApiSlice
    with ServerActorSlice {

    val port = Config(system).loginPort
    override lazy val actorSystem = system
    override lazy val localAddress = new InetSocketAddress(port)


    override def receive = super.receive orElse {
        case msg: GS2LS =>
            val gameSrv = sender
            messageBus.publish(gameSrv, msg)
    }
}