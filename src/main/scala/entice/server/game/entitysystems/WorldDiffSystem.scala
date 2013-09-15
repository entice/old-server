/**
 * For copyright information see the LICENSE document.
 */

package entice.server.game.entitysystems

import entice.server._
import entice.server.game._
import entice.server.utils._
import entice.protocol._
import entice.protocol.utils.MessageBus._

import akka.actor.{ Actor, ActorRef, ActorLogging }

import scala.concurrent.duration._


/**
 * Static logic holder
 */
object WorldDiffSystem {

    /**
     * Compares two worlds, resulting in a list with added and removed entities,
     * and a map that includes all the updates that were done on the entity sys
     */
    def worldDiff(
        last:    Map[Entity, Set[Component]], 
        current: Map[Entity, Set[Component]]):
        (List[Entity], List[Entity], Map[Entity, Set[Component]]) = {

        val addedEty   = current.keys.toList    diff last.keys.toList
        val removedEty = last.keys.toList       diff current.keys.toList
        val persistEty = last.keys.toList       intersect current.keys.toList

        def optDiff(a: Set[Component], b: Set[Component]): Option[Set[Component]] = {
            val result = a diff b
            return (if (result != Set.empty) Some(result) else None)
        }

        
        // get the diff of all persistent entities
        var diffs: Map[Entity, Set[Component]] =
        (for {
            e <- persistEty
            l <- last.get(e)
            c <- current.get(e)
            d <- optDiff (c, l)
        } yield (e -> d)).toMap

        // add the added entities and their comps to the diff
        var added: Map[Entity, Set[Component]] =
        (for {
            e <- addedEty
            c <- current.get(e)
        } yield (e -> c)).toMap

        return (addedEty, removedEty, diffs ++ added)
    }


    def worldCopy(world: Map[Entity, Set[Component]]): Map[Entity, Set[Component]] = {

        def setCopy(comps: Set[Component]): Set[Component] = (for (c <- comps) yield {c.clone}).toSet

        (for {
            (e, c) <- world
        } yield {
            (e -> setCopy(c))
        }).toMap
    }
}


/**
 * The actual system
 */
class WorldDiffSystem(
    val reactor: ActorRef,
    players: Registry[Player],
    entityMan: EntityManager) extends Actor with ActorLogging with Subscriber {

    import WorldDiffSystem._

    var lastWorldState: Map[Entity, Set[Component]] = Map()
    var lastDiffTime = System.nanoTime()

    val subscriptions =
        classOf[Tick] ::
        classOf[Flush] ::
        Nil


    override def preStart {
        register
        lastWorldState = worldCopy(entityMan.getAll)
    }


    def timeDelta = {
        val current = System.nanoTime()
        val millisecondsDiff = (current - lastDiffTime) / 1000000
        lastDiffTime = current

        millisecondsDiff.toInt
    }


    def receive = {
        case MessageEvent(_, Tick()) | MessageEvent(_, Flush()) =>
            // retrieve a deep copy of the world state
            val newWorldState = worldCopy(entityMan.getAll)

            val (added, removed, diffs) = worldDiff(lastWorldState, newWorldState)
            val views = EntityView(diffs)
            players.getAll 
                .filter  { _.state == Playing }
                .map     { _.session ! GameUpdate(timeDelta, views, added, removed)}
            lastWorldState = newWorldState
    }
}