/**
 * For copyright information see the LICENSE document.
 */

package entice.server.network

import entice.protocol._
import akka.actor.{ Actor, ActorRef, ActorLogging }
import akka.io.TcpPipelineHandler._
import akka.routing.Listeners


private[network] object Session {
    case class Pipeline(pipe: ActorRef)
}


/**
 * Manages a TCP session and does the serialization via its pipeline.
 * A server can listen to the events of this.
 */
private[network] class Session(
    pipeInit: Init[WithinActorContext, Message, Message],
    connection: ActorRef) extends Actor with ActorLogging with Listeners {

    import Grid._
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

        case OnlyReportTo(srv) =>
            listeners clear
            listeners add srv

        case c: ConnectionClosed =>
            log.info(s"Client disconnected. Terminating session...")
            context stop self
    }
}
