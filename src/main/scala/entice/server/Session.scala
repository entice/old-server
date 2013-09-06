/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol._
import entice.protocol.utils._

import akka.actor.{ Actor, ActorRef, ActorLogging, ActorSystem, Props, PoisonPill }
import akka.io.{ IO, Tcp, TcpPipelineHandler }
import akka.io.TcpPipelineHandler._
import akka.pattern.gracefulStop
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

import java.net.InetSocketAddress
import java.util.UUID


/**
 * Accepts TCP sessions and creates the appropriate session actors
 *
 * Hint: Can be wired, if the reactor is provided explicitly.
 */
class SessionAcceptorActor(
    localAddress: InetSocketAddress,
    reactor: ActorRef) 
        extends Actor with ActorLogging {


    import SessionActor._
    import Tcp._
    import context.system

    IO(Tcp) ! Bind(self, localAddress)

    var actualLocalAddress: Option[InetSocketAddress] = None 


    def receive = {
        case b @ Bound(local) =>
            actualLocalAddress = Some(local)

        case CommandFailed(_: Bind) =>
            context stop self

        case c @ Connected(remote, local) =>
            val init = PipelineFactory.getWithLog(log)
            val connection = sender
            val handler: ActorRef = context.actorOf(Props(classOf[SessionActor], init, connection), "session-" + UUID.randomUUID())
            val pipeline = context.actorOf(TcpPipelineHandler.props(init, connection, handler))

            handler ! Reactor(reactor)
            handler ! Pipeline(pipeline)
            connection ! Register(pipeline)
    }

    override def postStop {
        context.children map { 
            a: ActorRef =>
            try {
                val stopped: Future[Boolean] = gracefulStop(a, Duration(5, SECONDS))
                Await.result(stopped, Duration(6, SECONDS))
            } catch {
                case e: akka.pattern.AskTimeoutException => 
                    log.error("Couldnt stop session actor. {}", e)
                    a
            }
        }
    }
}


object SessionActor {
    case class Pipeline(pipe: ActorRef)
    case class Reactor(reactor: ActorRef)
}


/**
 * Manages a TCP session and does the serialization via its pipeline
 */
class SessionActor(
    pipeInit: Init[WithinActorContext, Message, Message],
    connection: ActorRef)
    extends Actor with ActorLogging {

    import SessionActor._
    import ReactorActor._
    import Tcp.{ Close, PeerClosed }

    var pipeline: Option[ActorRef] = None
    var reactor: Option[ActorRef] = None


    def receive = {
        // init with pipeline or reactor
        case Pipeline(pipe) =>
            pipeline = Some(pipe)

        case Reactor(react) =>
            reactor = Some(react)

        // handle new message
        case pipeInit.Event(data) =>
            log.info(s"Got: ${data.toString}")
            reactor map { _ ! Publish(self, data) }

        // handle outgoing message
        case data: Message =>
            log.info(s"Put: ${data.toString}")
            pipeline map { _ ! pipeInit.Command(data) }

        // client disconnected
        case PeerClosed => context stop self
    }

    override def postStop {
        connection ! Close
    }
}
