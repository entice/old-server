/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import Named._

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, ImplicitSender }
import com.typesafe.config.ConfigFactory

import org.scalatest._


class BehaviourSpec extends TestKit(ActorSystem(
    "behavs-spec-sys", 
    config = ConfigFactory.parseString("""
      akka {
        loglevel = WARNING,
        test.single-expect-default = 0
      }""")))
    with WordSpecLike 
    with MustMatchers 
    with BeforeAndAfterAll
    with OneInstancePerTest {

  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val actorSystem = system
  override def afterAll { TestKit.shutdownActorSystem(system) }


  import BehaviourSpec._

  "A behaviour factory" must {

    "only accept entities with specific components" in {
      val w = new World(system)
      val e1 = new Entity(w) + SomeAttr1()
      val e2 = new Entity(w) + SomeAttr1() + SomeAttr2()
      val e3 = new Entity(w) + SomeAttr1() + SomeAttr3()
      val s1 = SomeBehaviourFactory1
      val s2 = SomeBehaviourFactory2
      val s3 = SomeBehaviourFactory3

      s1.createFor(e1) must be(Some(SomeBehaviour1(e1)))
      s1.createFor(e2) must be(Some(SomeBehaviour1(e2)))
      s1.createFor(e3) must be(Some(SomeBehaviour1(e3)))

      s2.createFor(e1) must be(Some(SomeBehaviour2(e1)))
      s2.createFor(e2) must be(None)
      s2.createFor(e3) must be(Some(SomeBehaviour2(e3)))

      s3.createFor(e1) must be(None)
      s3.createFor(e2) must be(None)
      s3.createFor(e3) must be(Some(SomeBehaviour3(e3)))
    }
  }
}

object BehaviourSpec {
  case class SomeAttr1() extends Attribute
  case class SomeAttr2() extends Attribute 
  case class SomeAttr3() extends Attribute 

  case class SomeBehaviour1(val e: Entity) extends Behaviour(e)
  object SomeBehaviourFactory1 extends BehaviourFactory[SomeBehaviour1] {
    val requires = has[SomeAttr1] :: Nil
    val createInternal = SomeBehaviour1
  }

  case class SomeBehaviour2(val e: Entity) extends Behaviour(e)
  object SomeBehaviourFactory2 extends BehaviourFactory[SomeBehaviour2] {
    val requires = has[SomeAttr1] :: hasNot[SomeAttr2] :: Nil
    val createInternal = SomeBehaviour2
  }

  case class SomeBehaviour3(val e: Entity) extends Behaviour(e)
  object SomeBehaviourFactory3 extends BehaviourFactory[SomeBehaviour3] {
    val requires = has[SomeAttr1] :: has[SomeAttr3] :: Nil
    val createInternal = SomeBehaviour3
  }
}