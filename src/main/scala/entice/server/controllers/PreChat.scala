/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server._, Net._
import entice.server.world._
import entice.server.utils._
import entice.protocol._
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }


class PreChat extends Actor with Subscriber with Clients {

    val subscriptions = classOf[ChatMessage] :: Nil
    override def preStart { register }


    def receive = {
        case MessageEvent(session, ChatMessage(_, msg)) =>
            clients.get(session)  match {
                case Some(client) if client.state == Playing =>
                    publish(Chat(client.entity.get, msg))
                case _ =>
                    session ! Kick
            }
    }
}