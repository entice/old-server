/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server._, Net._
import entice.server.world._
import entice.server.utils._
import entice.server.database._
import entice.protocol._
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }


class CharDelete extends Actor with Subscriber with Clients {

    val subscriptions = classOf[entice.protocol.CharDelete] :: Nil
    override def preStart { register }


    def receive = {

        case MessageEvent(session, entice.protocol.CharDelete(entity)) =>
            clients.get(session)  match {
                case Some(client @ Client(_, _, chars, _, _, _)) 
                    if chars.contains(entity)
                    && client.state == IdleInLobby =>
                
                    Character.deleteByName(chars(entity).name)
                    client.chars = chars - entity
 
                case _ =>
                    session ! Failure("Ugly hacks detected! Muhahaha! Kicking session...")
                    session ! Kick
            }
    }
}