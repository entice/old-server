/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import akka.actor.{ Actor, ActorRef, ActorSystem, ActorLogging, Props }
import akka.io.{ IO, Tcp }

import java.net.InetSocketAddress


/**
 * Should be implemented by concrete session actors in the servers
 */
abstract class Session(connection: ActorRef, remote: InetSocketAddress) extends Actor


/**
 * Mixes in an optional session (as actor ref)
 * and an additional apply method, ideally into a message case class.
 *
 * Hint: use the apply method like a curried method
 */
trait SessionMixin {

    type T <: AnyRef

    var session: Option[ActorRef] = None

    def apply(sess: ActorRef): T = {
        session = Some(sess)
        this.asInstanceOf[T]
    }
}


/**
 * Accepts sessions on a certain port.
 */
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