/**
 * For copyright information see the LICENSE document.
 */

package entice.server.network

import akka.actor.{ Actor, ActorRef, ActorSystem }
import akka.io.{ IO, Tcp, TcpPipelineHandler }
import akka.io.TcpPipelineHandler._
import java.net.InetSocketAddress


private[network] object Acceptor {
    case class Start(addr: InetSocketAddress)
}


/**
 * Accepts TCP sessions and creates the appropriate session actors
 * A server can listen to the events of this.
 */
private[network] class Acceptor extends Actor with ActorLogging with Listeners {

    import Grid._
    import Acceptor._
    import Tcp._
    import context.system

    def receive = listenerManagement orElse {
        case Start(addr)            => IO(Tcp) ! Bind(self, addr)

        case Bound(addr)            => gossip(BindSuccess(addr))
        case CommandFailed(_: Bind) => gossip(BindFailure)

        case Connected(remote, local) =>
            val init = PipelineFactory.getWithLog(log)
            val connection = sender
            val handler: ActorRef = context.actorOf(Props(classOf[Session], init, connection))
            val pipeline = context.actorOf(TcpPipelineHandler.props(init, connection, handler))

            listeners foreach { handler ! Listen(_) }
            handler ! Pipeline(pipeline)
            connection ! Register(pipeline)

            gossip(NewSession(handler))
    }
}