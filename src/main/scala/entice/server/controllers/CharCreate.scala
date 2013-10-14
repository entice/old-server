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

        case MessageEvent(session, CharCreateRequest(name, appearance)) =>
            clients.get(session)  match {
                case Some(client) if client.state == IdleInLobby =>
                
                    Character.findByName(name) match {
                        case Some(_) => 
                            session ! Failure("Name invalid or taken.")

                        case None =>
                            Character.create(
                                Character(
                                    accountId = client.account.id, 
                                    name = name, 
                                    appearance = appearance))

                            val entity = Entity(UUID())
                            client.chars = client.chars + (entity -> ((name, appearance)))
                            session ! CharCreateSuccess(entity)
                    }
 
                case _ =>
                    session ! Failure("Not logged in, or not playing.")
                    session ! Kick
            }
    }
}