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

    val subscriptions = classOf[UpdateRequest] :: Nil
    override def preStart { register }

    def receive = {

        case MessageEvent(session, UpdateRequest(EntityView(_, view: MovementView))) => 
            clients.get(session) match {
                case Some(client) if client.state == Playing =>
                    client.entity map {_.set(view.position)}
                    client.entity map {_.set(view.movement)}
                    publish(Move(client.entity.get))
                    publish(Flush())
                case _ =>
                    session ! Kick
            }

        case MessageEvent(_, UpdateRequest(_)) => // we will get update requests that were not made for us...
    }
}