/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server._
import entice.server.utils._
import entice.server.database._
import entice.protocol._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }


/**
 * TODO: refactor me!
 */
class Login extends Actor with Subscriber with Clients with Worlds {

    val subscriptions = classOf[LoginRequest] :: Nil
    override def preStart { register }

    val emailPattern = """[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]"""

    def receive = {

        // login with valid email (weak check)
        case MessageEvent(session, LoginRequest(email, pwd)) if (email matches emailPattern) =>
            // check account and password
            val acc = Account.findByEmail(email) 
            acc match {

                case None =>
                    session ! Failure("Invalid login credentials.")
                case Some(_) if (acc.get.password != pwd)=>
                    session ! Failure("Invalid login credentials.")

                case Some(_) =>
                    val chars = Character.findByAccount(acc.get) 
                        .foldLeft(Map[Entity, CharacterView]()) { 
                            (chars, char) => chars + (Entity(UUID()) -> CharacterView(char.name, char.appearance)) 
                        }
                    val entityviews = (for ((e, charview) <- chars) yield EntityView(e,charview)).toList

                    val client = Client(session, acc.get, chars, worlds.default, state = IdleInLobby)
                    
                    clients.add(client)
                    session ! LoginSuccess(entityviews)
            }

        // invalid email
        case MessageEvent(session, _) => 
            session ! Failure("Invalid email format.")
    }
}