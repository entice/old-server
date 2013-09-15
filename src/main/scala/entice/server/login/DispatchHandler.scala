/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.server._
import entice.server.utils._
import entice.server.game._
import entice.server.Config._
import entice.protocol._
import entice.protocol.utils.MessageBus._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }


class DispatchHandler(
    val srvConfig: EnticeServer,
    val server: ActorRef,
    val reactor: ActorRef,
    val clients: Registry[Client]) extends Actor with Subscriber {

    import entice.server.ActorSlice._

    // TODO change me in other milestones...
    var gs: Option[ActorRef] = None

    val subscriptions =
        classOf[DispatchRequest] ::
        classOf[WaitingForPlayer] ::
        Nil


    override def preStart {
        register
    }


    def receive = {

        // we require the client to be logged in (available in the registry)
        case MessageEvent(Sender(uuid, session), DispatchRequest()) if (clients.get(uuid) != None) =>
            clients.get(uuid) map { c: Client =>
                val tempGs = gs.getOrElse(context.system.actorOf(Props(new GameServer(context.system, srvConfig.gamePort) with AutoStart)))
                server ! SendTo(tempGs, AddPlayer(uuid, c.gsKey))
                gs = Some(tempGs)
            }

        // if it is not, kick it
        case MessageEvent(Sender(uuid, session), DispatchRequest()) =>
            context.system stop session

        case MessageEvent(gs, WaitingForPlayer(uuid)) =>
            clients.get(uuid) map { c: Client =>
                c.session ! DispatchResponse(srvConfig.host, srvConfig.gamePort, c.gsKey)
            }
    }
}