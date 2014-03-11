/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server._, Net._
import entice.server.utils._
import entice.server.world._
import entice.server.physics._, Geometry._
import entice.protocol._, MoveState._
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }


class PreMovement extends Actor with Subscriber with Clients {

    val subscriptions = classOf[MoveRequest] :: Nil
    override def preStart { register }

    def receive = {

        case MessageEvent(session, MoveRequest(direction)) => 
            clients.get(session) match {

                // in case the entity wants to stop moving
                case Some(client) if client.state == Playing 
                                  && direction    == Coord2D(0, 0) =>
                    // update the entity movement state
                    client.entity map { e =>
                        e.set[Movement](e[Movement].copy(
                            goal  = e[Position].pos,
                            state = NotMoving.toString))
                    }
                    publish(Move(client.entity.get))
                    publish(PushUpdate())

                // in case the entity wants to move in some direction
                case Some(client) if client.state == Playing 
                                  && direction    != Coord2D(0, 0) =>
                    // update the entity movement state
                    client.entity map { e =>
                        e.set[Movement](e[Movement].copy(
                            goal  = e[Position].pos + direction,
                            state = Moving.toString))
                    }
                    publish(Move(client.entity.get))
                    publish(PushUpdate())

                case _ =>
                    session ! Failure("Not logged in, or not playing.")
                    session ! Kick
            }
    }
}
