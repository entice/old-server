/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol._
import entice.protocol.utils._
import akka.actor._
import akka.routing._
import akka.io.{ IO, Tcp, TcpPipelineHandler }
import akka.io.TcpPipelineHandler._
import scala.collection.JavaConversions._
import java.net.InetSocketAddress


/**
 * Implements an IO extension that encapsulates the entice protocol.
 */
object Net extends ExtensionKey[NetExt] with ExtensionIdProvider {

    sealed trait NetRequest extends Typeable
    case class Start(addr: InetSocketAddress)       extends NetRequest
    case object Kick                                extends NetRequest

    sealed trait NetResult extends Typeable
    case class BindSuccess(addr: InetSocketAddress) extends NetResult
    case object BindFailure                         extends NetResult

    sealed trait NetEvent extends Typeable
    case class NewSession(sess: ActorRef)           extends NetEvent
    case class LostSession(sess: ActorRef)          extends NetEvent
    case class NewMessage(msg: Message)             extends NetEvent
}


class NetExt(system: ExtendedActorSystem) extends akka.io.IO.Extension {
    val manager = system.actorOf(Props[NetActor], "net")
}


/**
 * Accepts TCP sessions and creates the appropriate session actors
 */
private class NetActor extends Actor with ActorLogging with Listeners {

    import Net._
    import Tcp._
    import Session._
    import context.system

    def receive = listenerManagement orElse {
        case Start(addr)            => 
            IO(Tcp) ! Bind(self, addr)
            val server = sender
            self ! Listen(server)

        case Bound(addr)            => gossip(BindSuccess(addr))
        case CommandFailed(_: Bind) => gossip(BindFailure)

        case Connected(remote, local) =>
            val init = PipelineFactory.getWithLog(log)
            val connection = sender
            val handler: ActorRef = context.actorOf(Props(classOf[Session], init, connection))
            val pipeline = context.actorOf(TcpPipelineHandler.props(init, connection, handler))

            listeners.toSet foreach {a: ActorRef => handler ! Listen(a) }
            handler ! Pipeline(pipeline)
            connection ! Register(pipeline)

            gossip(NewSession(handler))
            context watch handler

        case Terminated(_) =>
            val handler = sender
            gossip(LostSession(handler))
    }
}


private object Session {
    case class Pipeline(pipe: ActorRef)
}


/**
 * Manages a TCP session and does the serialization via its pipeline.
 * A server can listen to the events of this.
 */
private class Session(
    pipeInit: Init[WithinActorContext, Message, Message],
    connection: ActorRef) extends Actor with ActorLogging with Listeners {

    import Net._
    import Session._
    import Tcp.{ Close, ConnectionClosed }

    var pipeline: Option[ActorRef] = None


    def receive = listenerManagement orElse {
        case Pipeline(pipe) => pipeline = Some(pipe)

        case pipeInit.Event(data) => 
            log.debug(s"Got: ${data.toString}")
            gossip(NewMessage(data))

        case data: Message => 
            log.debug(s"Put: ${data.toString}")
            pipeline map { _ ! pipeInit.Command(data) }

        case Kick =>
            log.info(s"Client kicked. Terminating session...")
            context stop self

        case c: ConnectionClosed =>
            log.info(s"Client disconnected. Terminating session...")
            context stop self
    }
}
