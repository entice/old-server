/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server._, Net._
import entice.server.world._
import entice.server.utils._
import entice.protocol._
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

import scala.util.{ Try }


class PreChat extends Actor with Subscriber with Clients {

    val subscriptions = classOf[ChatMessage] :: Nil
    override def preStart { register }


    def receive = {
        case MessageEvent(session, ChatMessage(_, msg, chan)) =>
            clients.get(session)  match {
                
                case Some(client) 
                    if client.state == Playing
                    && Try(ChatChannels.withName(chan)).isSuccess =>

                    publish(Chat(client.entity.get, msg, ChatChannels.withName(chan)))
                
                case _ =>
                    session ! Failure("Not logged in, or not playing, or unkown channel.")
                    session ! Kick
            }
    }
}