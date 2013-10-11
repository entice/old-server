/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers
import entice.server._, Net._
import entice.server.utils._
import entice.protocol._
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import scala.collection.mutable


class Play extends Actor with Subscriber with Clients with Worlds {

    val subscriptions = 
        classOf[PlayRequest] :: 
        classOf[PlayChangeMap] ::
        classOf[PlayQuit] ::
        Nil

    override def preStart { register }


    def receive = {
        // client wants to play
        case MessageEvent(session, PlayRequest(entity)) =>
            clients.get(session) match {

                case Some(client @ Client(_, _, chars, world, _, state)) 
                    if chars.contains(entity) 
                    && state == IdleInLobby =>

                    client.entity = Some(world.use(entity, playerComps(chars(entity))))
                    session ! PlaySuccess(world.name, world.dump)
                    client.state = Playing

                case _ =>
                    session ! Failure("Ugly hacks detected! Muhahaha! Kicking session...")
                    session ! Kick
            }

        // client wants to change the map
        case MessageEvent(session, PlayChangeMap(newMap)) =>
            clients.get(session) match {

                case Some(client @ Client(_, _, chars, world, rich, state))
                    if rich.isDefined
                    && state == Playing =>

                    val entity = rich.get.entity
                    world.remove(entity)
                    client.world = worlds.get(newMap)
                    client.entity = Some(client.world.use(entity, playerComps(chars(entity))))
                    session ! PlaySuccess(client.world.name, client.world.dump)

                case _ =>
                    session ! Failure("Ugly hacks detected! Muhahaha! Kicking session...")
                    session ! Kick
            }

        // client wants to quit playing (back to charselection)
        case MessageEvent(session, PlayQuit()) =>
            clients.get(session) match {

                case Some(client @ Client(_, _, _, world, entity, state))
                    if state == Playing =>

                    world.remove(entity.get)
                    client.entity = None
                    client.state = IdleInLobby

                case _ =>
                    session ! Failure("Ugly hacks detected! Muhahaha! Kicking session...")
                    session ! Kick
            }
    }


    def playerComps(char: CharacterView) = {
        new TypedSet[Component]() 
            .add(char.name)
            .add(char.appearance)
            .add(Position())
            .add(Movement())
            .add(Animation())
    }
}