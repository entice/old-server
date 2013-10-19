/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server._, Net._
import entice.server.utils._
import entice.server.world._
import entice.protocol._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }


/**
 * Watches for disconnected clients to make sure they 
 * are being removed from the registry.
 */
class Disconnect extends Actor with Subscriber with Clients with Worlds {

    val subscriptions = classOf[LostSession] :: Nil
    override def preStart { register }

    def receive = {
        case MessageEvent(_, LostSession(session)) =>
            clients.get(session)  match {
                case Some(client) => 
                    client.entity foreach { e => 
                        e.world.remove(e.entity)
                    }
                    clients.remove(session)
                case _ =>
            }
    }
}