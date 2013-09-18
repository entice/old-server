/**
 * For copyright information see the LICENSE document.
 */

package entice.server.network

import entice.protocol._
import akka.actor._
import akka.io.IO._
import java.net.InetSocketAddress
import scala.collection._


/**
 * Implements an IO extension that encapsulates the entice protocol.
 */
object Grid extends ExtensionKey[GridExt] {

    sealed trait GridRequest
    case class Bind(addr: InetSocketAddress)        extends GridRequest

    sealed trait GridResult
    case class BindSuccess(addr: InetSocketAddress) extends GridResult
    case object BindFailure                         extends GridResult

    sealed trait AcceptorEvent
    case class NewSession(sess: ActorRef)           extends AcceptorEvent

    sealed trait SessionEvent
    case class NewMessage(msg: Message)             extends SessionEvent
    case class OnlyReportTo(me: ActorRef)           extends SessionEvent
}


class GridExt(system: ExtendedActorSystem) extends Extension {
    val manager = system.actorOf(Props[GridActor], "grid")
}


/**
 * Manages the whole network extension.
 */
private[network] class GridActor extends Actor {

    import Grid._

    val acceptors: mutable.Map[InetSocketAddress, ActorRef] = mutable.Map()

    def receive = {
        case Register(addr) => 
            val acc = acceptors.get(addr)
            val srv = sender
            if (acc.isDefined) { acc.get ! Listen(srv); srv ! BindSuccess }
            else               { newAcceptor(addr) ! Listen(srv) }

        case BindSuccess(addr) =>
            val acc = sender
            acceptors += (addr -> acc) // prevents us from adding a port 0 acceptor

        case BindFailure =>
            val acc = sender
            context stop acc
    }

    def newAcceptor(srv: ActorRef, addr: InetSocketAddress) {
        val acc = context.actorOf(Props[Acceptor])
        acc ! Listen(self)
        acc ! Start(addr)
    }
}