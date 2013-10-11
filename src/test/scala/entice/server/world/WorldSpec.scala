/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.world

import entice.server.utils._
import entice.protocol._

import shapeless._

import org.scalatest._
import org.scalatest.matchers._


class WorldSpec extends WordSpec with MustMatchers  {

    case class TestSystem(world: World) extends System[Name :: Position :: HNil] {
        def testExpect(ents: Set[RichEntity]) {
            entities(world) must be(ents)
        }
    }


    "A World" must {


        "create entities out of their components and retrieve these" in {
            val w = new World("testworld1")
            val c = new TypedSet[Component]() + Name() + Position() + Movement()
            val e = w.create(c)
            w.getRich(e.entity) must be(e)
            w.getComps(e.entity) must be(c)
        }


        "remove entities" in {
            val w = new World("testworld2")
            val c = new TypedSet[Component]() + Name() + Position() + Movement()
            val e = w.create(c)
            w.remove(e.entity)
            intercept[NoSuchElementException] {
                w.getRich(e.entity)
            }
        }


        "create correct world diffs" in {
            // (does not involve the actorsystem)
            // given
            val w = new World("testworld3")
            val et1, et2, et3, et4 = Entity(UUID())
            
            // step 1
            w.use(et1, new TypedSet[Component]() + Name("et1") + Position())
            w.use(et2, new TypedSet[Component]() + Position())
            w.use(et3, new TypedSet[Component]() + Movement()) // actually never happens
            // create a diff that we dont want right now, just to flush the stuff out
            w.diff

            // step 2
            w.update(et1, new TypedSet[Component]() + Name("et1-new") + Position())
            w.update(et2, new TypedSet[Component]() + Position(Coord2D(1, 1)))
            w.remove(et3)
            w.use   (et4, new TypedSet[Component]() + Position() + Movement()) // replace et3 with et4

            val expectedDiff = List(
                EntityView(et1, AllCompsView(List(Name("et1-new")))),
                EntityView(et2, AllCompsView(List(Position(Coord2D(1, 1))))),
                EntityView(et4, AllCompsView(List(Position(), Movement())))) // also expect new entities

            // when
            val (diffs, added, removed) = w.diff

            // must
            added must be(List(et4))
            removed must be(List(et3))
            diffs must be(expectedDiff)
        }


        "register systems and check if they want to accept any entities" in {
            val w = new World("testworld4")
            val s = TestSystem(w)
            val c1 = new TypedSet[Component]() + Name() + Position() + Movement()
            val c2 = new TypedSet[Component]() + Name() + Movement()
            val e1 = w.create(c1)
            val e2 = w.create(c2)
            s.testExpect(Set(e1))
        }
    }
}
