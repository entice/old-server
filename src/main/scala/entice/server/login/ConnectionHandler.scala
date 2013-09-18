/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.server._
import entice.server.utils._
import entice.protocol._
import entice.protocol.utils.MessageBus._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }


class ConnectionHandler(
    val messageBus: MessageBus,
    val clients: Registry[Client],
    val serverActor: ActorRef) extends Actor with Subscriber {

    import SessionActor._

    val emailPattern = """[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]"""

    val subscriptions =
        classOf[LoginRequest] ::
        Nil


    override def preStart {
        register
    }


    def receive = {

        // login with valid email (weak check)
        case MessageEvent(Sender(uuid, session), LoginRequest(email, pwd)) if (email matches emailPattern) =>
            // TODO: init account DAO, check creds, create client,
            clients.add(Client(uuid, session))
            session ! OnlyReportTo(serverActor)
            session ! LoginSuccess()
            context watch session

        // invalid email
        case MessageEvent(Sender(uuid, session), l: LoginRequest) => 
            session ! LoginFail("Invalid email format.")

        // a session terminated
        case Terminated(_) =>
            val sess = sender
            clients.remove(id)
    }
}