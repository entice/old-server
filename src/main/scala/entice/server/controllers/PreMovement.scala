/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server._, Net._
import entice.server.utils._
import entice.server.world._
import entice.protocol._
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }


class PreMovement extends Actor with Subscriber with Clients {

    val subscriptions = classOf[MoveRequest] :: Nil
    override def preStart { register }

    def receive = {

        case MessageEvent(session, MoveRequest(pos, move)) => 
            clients.get(session) match {

                case Some(client) if client.state == Playing =>
                    client.entity map {_.set(pos)}
                    client.entity map {_.set(move)}
                    publish(Move(client.entity.get))
                    publish(Flush())

                case _ =>
                    session ! Failure("Not logged in, or not playing.")
                    session ! Kick
            }
    }
}