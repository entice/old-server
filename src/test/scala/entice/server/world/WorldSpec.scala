/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.world

import entice.server.utils._
import entice.protocol._
import akka.actor._
import akka.testkit._

import shapeless._

import org.scalatest._
import org.scalatest.matchers._


class WorldSpec 
    extends WordSpec 
    with MustMatchers
    with OneInstancePerTest  {

    case class TestSystem(world: World) extends System[Name :: Position :: HNil] {
        def testExpect(ents: Set[RichEntity]) {
            entities(world) must be(ents)
        }
    }


    "A World" must {


        "create entities out of their components and retrieve these" in {
            val w = new World("testworld1", new MessageBus)
            val c = new TypedSet[Component]() + Name() + Position() + Movement()
            val e = w.create(c)
            w.getRich(e.entity) must be(Some(e))
            w.getComps(e.entity) must be(Some(c))
        }


        "remove entities" in {
            val w = new World("testworld2", new MessageBus)
            val c = new TypedSet[Component]() + Name() + Position() + Movement()
            val e = w.create(c)
            w.remove(e.entity)
            w.getRich(e.entity) must be(None)
        }


        "create correct world diffs" in {
            // (does not involve the actorsystem)
            // given
            val w = new World("testworld3", new MessageBus)
            val et1, et2, et3, et4 = Entity(UUID())
            
            // step 1
            w.use   (et1, new TypedSet[Component]() + Name("et1-1") + Position())
            w.use   (et2, new TypedSet[Component]() + Movement())
            w.use   (et3, new TypedSet[Component]() + Movement()) // actually never happens
            // create a diff that we dont want right now, just to flush the stuff out
            w.diff

            // step 2
            w.update(et1, new TypedSet[Component]() + Name("et1-2") + Position() + Movement())
            w.update(et2, new TypedSet[Component]() + Position(Coord2D(1, 1)))
            w.use   (et4, new TypedSet[Component]() + Position()) // replace et3 with et4
            w.remove(et3)

            var expectedDiff = List(
                EntityView(et1, List(Name("et1-2")), List(Movement()),              Nil),
                EntityView(et2, Nil,                 List(Position(Coord2D(1, 1))), List("Movement")),
                EntityView(et4, Nil,                 List(Position()),              Nil))

            // when
            {
                val (diffs, added, removed) = w.diff

                // must
                added must be(List(et4))
                removed must be(List(et3))
                diffs must be(expectedDiff)
            }

            // step 3 & 4
            w.update(et1, new TypedSet[Component]() + Name("et1-3") + Position()) // removed movement
            w.update(et2, new TypedSet[Component]() + Position(Coord2D(1, 1)) + Movement()) // added movement 
            w.update(et4, new TypedSet[Component]() + Position(Coord2D(1, 1)) + Movement()) // changed 1

            w.update(et1, new TypedSet[Component]() + Name("et1-4") + Position() + Movement()) // added movement
            w.update(et2, new TypedSet[Component]() + Position(Coord2D(1, 1))) // removed movement
            w.update(et4, new TypedSet[Component]() + Position() + Movement()) // reverted 1

            expectedDiff = List(
                EntityView(et1, List(Name("et1-4")),            Nil,                            Nil),
                EntityView(et2, Nil,                            Nil,                            Nil),
                EntityView(et4, Nil,                            List(Movement()),               Nil)) // also expect new entities

            // when
            {
                val (diffs, added, removed) = w.diff

                // must
                added must be(Nil)
                removed must be(Nil)
                diffs must be(expectedDiff)
            }
        }


        "register systems and check if they want to accept any entities" in {
            val w = new World("testworld4", new MessageBus)
            val s = TestSystem(w)
            val c1 = new TypedSet[Component]() + Name() + Position() + Movement()
            val c2 = new TypedSet[Component]() + Name() + Movement()
            val e1 = w.create(c1)
            val e2 = w.create(c2)
            s.testExpect(Set(e1))
        }


        "produce spawn and despawn messages" in {
            // given
            val system = ActorSystem("world-spec-sys")
            val m = new MessageBus
            val p = new TestProbe(system)
            m.subscribe(p.ref, classOf[Spawned])
            m.subscribe(p.ref, classOf[Despawned])

            val w = new World("testworld2", m)
            val c = new TypedSet[Component]() + Name() + Position() + Movement()
            
            // when 
            val e = w.create(c)
            // must
            p.expectMsgPF() {
                case MessageEvent(_, Spawned(e)) => true
            }

            // when 
            w.remove(e.entity)
            // must
            p.expectMsgPF() {
                case MessageEvent(_, Despawned(e)) => true
            }

            system.shutdown
        }
    }
}
