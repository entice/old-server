/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import Named._
import util.ReactiveTypeMap
import events.Evt
import test.MockEntity

import akka.actor.{ Actor, Props, ActorSystem }
import akka.testkit.{ TestKit, TestProbe, ImplicitSender }
import com.typesafe.config.ConfigFactory

import org.scalatest._

import scala.concurrent.duration._
import scala.language.postfixOps


class WorldSpec extends TestKit(ActorSystem(
    "world-spec-sys", 
    config = ConfigFactory.parseString("""
      akka {
        loglevel = WARNING,
        test.single-expect-default = 0
      }""")))
    with WordSpecLike 
    with MustMatchers 
    with BeforeAndAfterAll
    with OneInstancePerTest
    with ImplicitSender {

  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val actorSystem = system
  override def afterAll { TestKit.shutdownActorSystem(system) }

  import WorldSpec._

  trait WorldScope {
    class SomeWorld(system: ActorSystem) extends World(system) { 
      override def behaviours = SomeBehaviourFactory :: super.behaviours 
    }

    val world = new SomeWorld(system)
    val secondWorld = new SomeWorld(system)
  }

  "A world" must {

    "contain no entities when empty" in new WorldScope {
      val tester = MockEntity()
      world.contains(tester) must be(false)
    }

    "create entities with no attributes properly" in new WorldScope {
      val tester = world.createEntity()
      world.contains(tester) must be(true)
      tester.has[SomeAttr] must be(false)
    }

    "create entities with attributes properly" in new WorldScope {
      val tester = world.createEntity(Some(new ReactiveTypeMap[Attribute]().add(SomeAttr())))
      world.contains(tester) must be(true)
      tester.has[SomeAttr] must be(true)
    }

    "transfer entities properly" in new WorldScope {
      // create an entity
      val tester = world.createEntity()
      tester + SomeAttr()
      world.contains(tester) must be(true)
      secondWorld.contains(tester) must be(false)
      tester.has[SomeAttr] must be(true)
      // now transfer it
      val newTester = secondWorld.transferEntity(tester)
      world.contains(tester) must be(false)
      world.contains(newTester) must be(false)
      secondWorld.contains(tester) must be(false)
      secondWorld.contains(newTester) must be(true)
      newTester.has[SomeAttr] must be(true)
    }

    "remove entities properly" in new WorldScope {
      val tester = world.createEntity()
      world.contains(tester) must be(true)
      world.removeEntity(tester)
      world.contains(tester) must be(false)
    }

    "apply behaviours on attribute change" in new WorldScope { 
      // Note: world contains the testing behaviour (see below)
      val tester = world.createEntity() 
      tester.hasBehaviour[SomeBehaviour] must be(false)
      tester + SomeAttr()
      within(3 seconds) {
        tester.hasBehaviour[SomeBehaviour] must be(true)
      }
    }

    "remove behaviours on attribute removal" in new WorldScope { 
      // Note: world contains the testing behaviour (see below)
      val tester = world.createEntity(Some(new ReactiveTypeMap[Attribute]().add(SomeAttr())))
      tester.has[SomeAttr] must be(true)
      tester.hasBehaviour[SomeBehaviour] must be(true)
      tester.remove[SomeAttr]
      within(3 seconds) {
        tester.hasBehaviour[SomeBehaviour] must be(false)
      }
    }
  }
}


object WorldSpec {
  case class SomeAttr() extends Attribute

  case class SomeBehaviour(val e: Entity) extends Behaviour(e)
  object SomeBehaviourFactory extends BehaviourFactory[SomeBehaviour] {
    val requires = has[SomeAttr] :: Nil
    val creates  = SomeBehaviour
  }
}