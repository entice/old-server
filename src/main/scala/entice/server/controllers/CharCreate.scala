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


class CharCreate extends Actor with Subscriber with Clients {

    val subscriptions = classOf[CharCreateRequest] :: Nil
    override def preStart { register }


    def receive = {

        case MessageEvent(session, CharCreateRequest(charview)) =>
            clients.get(session)  match {
                case Some(client) if client.state == IdleInLobby =>
                
                    Character.findByName(charview.name) match {
                        case Some(_) => 
                            session ! CharCreateFail("Name invalid or taken.")

                        case None =>
                            Character.create(
                                Character(
                                    accountId = client.account.id, 
                                    name = charview.name, 
                                    appearance = charview.appearance))

                            val entity = Entity(UUID())
                            client.chars = client.chars + (entity -> charview)
                            session ! CharCreateSuccess(entity)
                    }

                case Some(client) =>
                    session ! CharCreateFail("Invalid client state. You can't create characters while playing or the like.")
                    
                case _ =>
                    session ! CharCreateFail("Ugly hacks detected! Muhahaha! Kicking session...")
                    session ! Kick
            }
    }
}