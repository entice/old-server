/**
 * For copyright information see the LICENSE document.
 */

package entice.server.game

import entice.server._
import entice.server.game.entitysystems._
import entice.protocol._
import entice.protocol.utils.MessageBus.MessageEvent

import akka.actor._
import akka.testkit._

import org.scalatest._
import org.scalatest.matchers._


class WorldDiffSystemSpec extends WordSpec with MustMatchers  {

    "An WorldDiffSystem" must {

        "create the correct diff of two worlds" in {
            // (does not involve the actorsystem)
            // given
            val et1, et2, et3, et4 = Entity(UUID())
            val world1: Map[Entity, Set[Component]] = Map(
                (et1 -> Set(Name("et1"), Position())),
                (et2 -> Set(Position())),
                (et3 -> Set())) // actually never happens
            val world2: Map[Entity, Set[Component]] = Map(
                (et1 -> Set(Name("et1-new"), Position())),
                (et2 -> Set(Position(Coord2D(1, 1)))),
                (et4 -> Set())) // replaced et3 with et4
            val expectedDiff: Map[Entity, Set[Component]] = Map(
                (et1 -> Set(Name("et1-new"))),
                (et2 -> Set(Position(Coord2D(1, 1)))),
                (et4 -> Set())) // also expect new entities
            // when
            val (added, removed, diffs) = WorldDiffSystem.worldDiff(world1, world2)
            // must
            added must be(List(et4))
            removed must be(List(et3))
            diffs must be(expectedDiff)
        }
    }
}