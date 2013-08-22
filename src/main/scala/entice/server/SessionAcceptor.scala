/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.io._

import java.net.InetSocketAddress


abstract class Session(connection: ActorRef, remote: InetSocketAddress) extends Actor


class SessionAcceptor(
    sessClazz: Class[_ <: Session],
    bindTo: InetSocketAddress) extends Actor with ActorLogging {
 
    import Tcp._
    import context.system

    IO(Tcp) ! Bind(self, bindTo)

    def receive = {
        // new client connected
        case c @ Connected(remote, local) =>
            val handler = context.actorOf(Props(sessClazz, sender, remote))
            sender ! Register(handler)

        // bind on startup failed
        case CommandFailed(_: Bind) => context stop self
    }
 
}