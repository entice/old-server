/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers
import entice.server._, Net._
import entice.server.utils._
import entice.protocol._
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import scala.collection.mutable


/**
 * TODO: refactor me!
 */
class Play extends Actor with Subscriber with Clients with Worlds {

    val subscriptions = classOf[PlayRequest] :: Nil
    override def preStart { register }


    def receive = {
        // clients wants to play
        case MessageEvent(session, PlayRequest(entity)) =>
            clients.get(session) match {
                case Some(client) if client.chars.contains(entity) && client.state == IdleInLobby =>
                    val c = client.chars(entity)
                    val e = worlds.get(client).use(
                        entity, 
                        new TypedSet[Component]() + c.name + c.appearance + Position() + Movement() + Animation())
                    client.entity = Some(e)
                    session ! PlaySuccess(worlds.get(client).dump)
                    client.state = Playing
                case _ =>
                    session ! PlayFail("Ugly hacks detected! Muhahaha! Kicking session...")
                    session ! Kick
            }
    }
}