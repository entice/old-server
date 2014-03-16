/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server._
import entice.server.utils._
import entice.server.database._
import entice.protocol._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import scala.language.postfixOps


class Login extends Actor with Subscriber with Clients with Worlds {

    val subscriptions = classOf[LoginRequest] :: Nil
    override def preStart { register }

    val emailPattern = """[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]"""

    def receive = {

        case MessageEvent(session, LoginRequest(email, pwd)) 
            if ((email matches emailPattern)
            &&  (clients.getAll find {_.account.email == email}) == None) =>
            // check account and password
            Account.findByEmail(email) match {
                case Some(acc) if (acc.password == pwd) =>
                    // get the chars of the acc, transfrom them and create a new client
                    val chars: Map[Entity, (Name, Appearance)] = 
                        (for (char <- Character.findByAccount(acc)) yield
                            (Entity(UUID()) -> ((char.name, char.appearance)))) 
                        .toMap
                    val entityviews = (for ((e, c) <- chars) yield EntityView(e, Nil, List(c _1, c _2), Nil)).toList
                    val client = Client(session, acc, chars, worlds.default, state = IdleInLobby)
                    
                    // register and inform the actual client
                    clients.add(client)
                    session ! LoginSuccess(entityviews)

                case _ =>
                    session ! Failure("Invalid login credentials.")
            }

        case MessageEvent(session, _) => 
            session ! Failure("Invalid email format, or account in use.")
    }
}