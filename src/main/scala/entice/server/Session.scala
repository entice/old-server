/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol._
import entice.protocol.utils._
import entice.protocol.utils.MessageBus._

import akka.actor.{ Actor, ActorRef, ActorLogging, ActorSystem, Props, PoisonPill }
import akka.io.{ IO, Tcp, TcpPipelineHandler }
import akka.io.TcpPipelineHandler._

import java.net.InetSocketAddress


private object SessionAcceptorFactory extends Multiton[(ActorSystem, InetSocketAddress), ActorRef] with Function2[ActorSystem, InetSocketAddress, ActorRef] {

    override protected val create = (p: (ActorSystem, InetSocketAddress)) => {
        val uuid = java.util.UUID.randomUUID()
        p._1.actorOf(Props(classOf[SessionAcceptorActor], p._2), s"session-acceptor-${uuid.toString}")
    }

    override def apply(a: ActorSystem, i: InetSocketAddress) = apply((a, i))
}


/**
 * Accepts TCP sessions and creates the appropriate session actors
 */
class SessionAcceptorActor(localAddress: InetSocketAddress) extends Actor with ActorLogging {

    import SessionActor._
    import ReactorActor._
    import MetaReactorActor._
    import NetSlice._
    import Tcp._
    import context.system

    IO(Tcp) ! Bind(self, localAddress)

    val metaReactor = context.actorOf(Props[MetaReactorActor])


    def receive = {

        case a: AddReactor =>
            metaReactor forward a

        case Bound(localAddress) =>
            metaReactor ! Publish(Sender(actor = self), BindSuccess(localAddress))

        case CommandFailed(_: Bind) =>
            metaReactor ! Publish(Sender(actor = self), BindFail())
            context stop self

        case Connected(remote, local) =>
            val init = PipelineFactory.getWithLog(log)
            val connection = sender
            val juuid = java.util.UUID.randomUUID()
            val uuid = UUID(juuid)
            val handler: ActorRef = context.actorOf(Props(classOf[SessionActor], init, connection, uuid), s"session-${juuid.toString}")
            val pipeline = context.actorOf(TcpPipelineHandler.props(init, connection, handler))

            handler ! Reactor(metaReactor)
            handler ! Pipeline(pipeline)
            connection ! Register(pipeline)
    }
}


object SessionActor {
    case class Pipeline(pipe: ActorRef)
    case class Reactor(reactor: ActorRef)
    case class NewUUID(uuid: UUID)
}


// TODO: better place for this?
case class SessionDisconnect extends Message


/**
 * Manages a TCP session and does the serialization via its pipeline
 * This will be initialized with a metareactor.
 * If a server instance actually wants to have this session for its own,
 * then needs to resend the Reactor message.
 */
class SessionActor(
    pipeInit: Init[WithinActorContext, Message, Message],
    connection: ActorRef,
    var uuid: UUID) extends Actor with ActorLogging {

    import SessionActor._
    import ReactorActor._
    import Tcp.{ Close, Closed, PeerClosed }

    var pipeline: Option[ActorRef] = None
    var reactor: Option[ActorRef] = None


    def receive = {
        // init or change stuff of this actor
        case Pipeline(pipe) =>
            pipeline = Some(pipe)
        case Reactor(react) =>
            reactor = Some(react)
        case NewUUID(id) =>
            uuid = id

        // handle new or outgoing message
        case pipeInit.Event(data) =>
            log.info(s"Got: ${data.toString}")
            reactor map { _ ! Publish(Sender(uuid, self), data) }
        case data: Message =>
            log.info(s"Put: ${data.toString}")
            pipeline map { _ ! pipeInit.Command(data) }

        // client disconnected
        case Closed | PeerClosed =>
            log.info(s"Client disconnected. Terminating session...")
            context stop self
    }

    override def postStop {
        reactor map { _ ! Publish(Sender(uuid, self), SessionDisconnect()) }
        connection ! Close
    }
}
