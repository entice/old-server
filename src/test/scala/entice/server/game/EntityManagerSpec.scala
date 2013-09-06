
package entice.server.game

import entice.protocol._

import org.scalatest._
import org.scalatest.matchers._


class EntityManagerSpec extends WordSpec with MustMatchers  {


    "An EntityManager" must {


        "be able to register entities" in {
            // given
            val em = new EntityManager
            val et = Entity(UUID())
            val name = Name("blubb")
            // must
            em.register(et, name)
        }


        "be able to unregister regsitered entities" in {
            // given
            val em = new EntityManager
            val et = Entity(UUID())
            val name = Name("blubb")
            // must
            em.register(et, name)
            em.unregister(et)
        }


        "be able to get components by entity and type" in {
            // given
            val em = new EntityManager
            val et1, et2, et3 = Entity(UUID())
            val (name1, name2, name3)  = (Name("1"), Name("2"), Name("3"))
            val (pos1, pos2) = (Position(), Position())
            val move1 = Movement()
            // when
            em + (et1, name1) + (et1, pos1) + (et1, move1)
            em + (et2, name2) + (et2, pos2)
            em + (et3, name3)
            // must
            em.getCompBy(et1, classOf[Name])        must be(Some(name1))
            em.getCompBy(et1, classOf[Position])    must be(Some(pos1))
            em.getCompBy(et1, classOf[Movement])    must be(Some(move1))

            em.getCompBy(et2, classOf[Name])        must be(Some(name2))
            em.getCompBy(et2, classOf[Position])    must be(Some(pos2))
            em.getCompBy(et2, classOf[Movement])    must be(None)

            em.getCompBy(et3, classOf[Name])        must be(Some(name3))
            em.getCompBy(et3, classOf[Position])    must be(None)
            em.getCompBy(et3, classOf[Movement])    must be(None)
        }


        "be able to get all components of an entity" in {
            // given
            val em = new EntityManager
            val et1, et2, et3, et4 = Entity(UUID())
            val (name1, name2, name3)  = (Name("1"), Name("2"), Name("3"))
            val (pos1, pos2) = (Position(), Position())
            val move1 = Movement()
            // when
            em + (et1, name1) + (et1, pos1) + (et1, move1)
            em + (et2, name2) + (et2, pos2)
            em + (et3, name3)
            // must
            em.getCompsOf(et1) must be(Some(Set(name1, pos1, move1)))
            em.getCompsOf(et2) must be(Some(Set(name2, pos2)))
            em.getCompsOf(et3) must be(Some(Set(name3)))
            em.getCompsOf(et4) must be(None)
        }


        "be able to get the entity of a component" in {
            // given
            val em = new EntityManager
            val et1, et2 = Entity(UUID())
            val (name1, name2, name3) = (Name("1"), Name("2"), Name("3"))
            // when
            em + (et1, name1)
            em + (et2, name2)
            // must
            em.getEntityOf(name1) must be(Some(et1))
            em.getEntityOf(name2) must be(Some(et2))
            em.getEntityOf(name3) must be(None)
        }


        "be able to get all entities that have at least certain component types" in {
            // given
            val em = new EntityManager
            val et1, et2 = Entity(UUID())
            val (name1, name2)  = (Name("1"), Name("2"))
            val pos1 = Position()
            // when
            em + (et1, name1) + (et1, pos1)
            em + (et2, name2)
            // must
            em.getEntitiesWith(classOf[Name])                                           must be(Set(et1, et2))
            em.getEntitiesWith(classOf[Name], classOf[Position])                        must be(Set(et1))
            em.getEntitiesWith(classOf[Name], classOf[Position], classOf[Movement])     must be(Set())
        }
    }
}