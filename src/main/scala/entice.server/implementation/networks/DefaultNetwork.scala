/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.networks

import entice.server._
import entice.server.macros._
import entice.server.implementation.events._

import entice.protocol._
import entice.protocol.utils._

import akka.actor._
import akka.routing._
import akka.io.{ IO, Tcp, TcpPipelineHandler }
import akka.io.TcpPipelineHandler._

import scala.collection.JavaConversions._
import java.net.InetSocketAddress


trait DefaultNetwork extends Network { selfRef: Core with Logger =>

  def serverHost: String
  def serverPort: Int

  object network extends NetworkLike {
    private var netActor: Option[ActorRef] = None

    def init() {
      selfRef.logger.info("Starting the server's network layer.")
      netActor = Some(actorSystem.actorOf(Props(NetworkActor(serverPort)), "network-socket"))
    }

    def shutdown() {
      selfRef.logger.info("Disconnecting the server's network layer.")
      netActor.map(_ ! NetworkShutdown)
    }
  }


  /** Send to gracefully shutdown the network actor and children */
  private case object NetworkShutdown

  /** Accepts TCP sessions and creates the appropriate session actors */
  private case class NetworkActor(port: Int) extends Actor {
    import Tcp._
    import context._

    IO(Tcp) ! Bind(self, new InetSocketAddress(port))

    def receive = {
      // Network binding
      case Bound(addr) =>
        selfRef.logger.info(s"Successfully bound to ${addr}")

      case CommandFailed(_: Bind) =>
        selfRef.logger.error(s"Failed to bind to the port ${port}, terminating network service.")
        network.shutdown

      // Client management
      case Connected(netAddr, _) =>
        selfRef.logger.info(s"A new client connected: ${netAddr.toString}")
        val client = sender
        val handler = context.actorOf(Props(SessionHandler(client)))
        selfRef.eventBus.pub(NewSession(handler))

      // Shutdown
      case NetworkShutdown =>
        become(dead)
        context.children.map(_ ! KickSession())
        context.stop(self)
    }

    def dead: Receive = {
      case msg => selfRef.logger.debug(s"Dead network manager received message: ${msg}")
    }
  }


  /** Handles de/serialization */
  private case class SessionHandler(remote: ActorRef) extends Actor with ActorLogging {
    import Tcp.{ Close, ConnectionClosed, Register }
    import context._

    // Subscriptions from the cake
    selfRef.eventBus.sub[KickSession]

    // Init the de/serialization pipeline
    val pipeInit = PipelineFactory.getWithLog(log); import pipeInit.{ Event, Command }
    val pipeline = context.actorOf(TcpPipelineHandler.props(pipeInit, remote, self))
    remote ! Register(pipeline)

    def receive = {
      // From network through pipeline
      case Event(data) =>
        selfRef.logger.info(s"Got: ${data.toString}")
        selfRef.eventBus.pub(data)

      // From internal, meant to be sent
      case msg: Message =>
        selfRef.logger.info(s"Put: ${msg.toString}")
        pipeline ! Command(msg)

      case KickSession =>
        become(dead)
        remote ! Close
        selfRef.logger.info("Session Kicked.")
        selfRef.eventBus.pub(LostSession(self))
        context.stop(self)

      case c: ConnectionClosed =>
        become(dead)
        selfRef.logger.info("Session disconnected.")
        selfRef.eventBus.pub(LostSession(self))
        context.stop(self)
    }

    def dead: Receive = {
      case msg => selfRef.logger.debug(s"Dead session received message: ${msg}")
    }
  }
}
