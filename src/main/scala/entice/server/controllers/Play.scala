/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers
import entice.server._, Net._
import entice.server.utils._
import entice.server.world._
import entice.protocol._
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import scala.collection.mutable
import scala.language.postfixOps


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

                    client.entity = Some(world.use(entity, playerComps(chars(entity), world.name)))
                    session ! PlaySuccess(world.name, toEntityView(world.dump))
                    client.state = Playing

                    publish(Spawned(client.entity.get))

                case _ =>
                    session ! Failure("Not logged in, or not idle in lobby.")
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
                    publish(Despawned(rich.get)) 

                    client.world = worlds.get(newMap)
                    client.entity = Some(client.world.use(entity, playerComps(chars(entity), client.world.name)))
                    session ! PlaySuccess(client.world.name, toEntityView(client.world.dump))

                    publish(Spawned(client.entity.get))

                case _ =>
                    session ! Failure("Not logged in, or not playing.")
                    session ! Kick
            }

        // client wants to quit playing (back to charselection)
        case MessageEvent(session, PlayQuit()) =>
            clients.get(session) match {

                case Some(client @ Client(_, _, _, world, entity, state))
                    if state == Playing =>

                    world.remove(entity.get)
                    publish(Despawned(entity.get)) 

                    client.entity = None
                    client.state = IdleInLobby

                case _ =>
                    session ! Failure("Not logged in, or not playing.")
                    session ! Kick
            }
    }


    def playerComps(char: (Name, Appearance), map: String) = {
        new TypedSet[Component]() 
            .add(char _1)
            .add(char _2)
            .add(Position(Maps.withMapName(map).spawns(0)))
            .add(Movement())
            .add(Animation())
    }

    def toEntityView(dump: Map[Entity, TypedSet[Component]]): List[EntityView] = {
        (for ((e, c) <- dump) yield
         EntityView(e, Nil, c.toList, Nil))
        .toList
    }
}