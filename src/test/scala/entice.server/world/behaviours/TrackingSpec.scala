/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world
package behaviours

import entice.server.Named
import entice.server.events._

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, ImplicitSender }
import com.typesafe.config.ConfigFactory

import scala.language.postfixOps
import scala.concurrent.duration._
import org.scalatest._


class TrackerSpec extends TestKit(ActorSystem(
    "tracker-spec-sys", 
    config = ConfigFactory.parseString("""
      akka {
        loglevel = WARNING
      }""")))//test.single-expect-default = 0
    with WordSpecLike 
    with MustMatchers 
    with BeforeAndAfterAll
    with OneInstancePerTest
    with ImplicitSender 

    with entice.server.world.Tracker {
  
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val actorSystem = system
  override def afterAll { TestKit.shutdownActorSystem(system) }


  import TrackerSpec._

  case class Track(entity: Entity, upd: Update)
  override def trackMe(entity: Entity, upd: Update) = self ! Track(entity, upd)

  "A tracking behaviour for entities" must {
    
    "receive attribute changes" in {
      val w = new World(system, tracker = this)
      val s1 = SomeComp1("Test")
      val s2 = SomeComp2(1337)
      val s3 = SomeComp3(false)
      val e = (new Entity(w) with EntityTracker) + Vision() + s1 + s2 // no s3.
      
      val t = TrackingFactory.createFor(e)
      t must be(Some(Tracking(e)))

      e.remove[SomeComp1]
      expectMsg(Track(e, AttributeRemove(e, s1)))

      e.set(SomeComp2(42))
      expectMsg(Track(e, AttributeChange(e, s2, SomeComp2(42))))

      e.add(s3)
      expectMsg(Track(e, AttributeAdd(e, s3)))
    }


    "receive entity changes" in {
      val w1 = new World(system, tracker = this)
      val w2 = new World(system, tracker = this)
      val e1 = w1.createEntity()
      val e2 = w1.createEntity()
      val e3 = w2.createEntity()
      val tester1 = w1.createEntity() + Vision(Set(e1)) // doesnt see e2
      val tester2 = w2.createEntity() + Vision(Set(e3)) // doesnt see e2
      
      val t1 = TrackingFactory.createFor(tester1)
      t1 must be(Some(Tracking(tester1)))

      val t2 = TrackingFactory.createFor(tester2)
      t2 must be(Some(Tracking(tester2)))

      w1.remove(e1)
      expectMsg(Track(tester1, EntityRemove(e1)))

      w1.remove(e2)
      expectNoMsg

      val e4 = w1.transfer(e3)
      expectMsgAnyOf(Track(tester1, EntityAdd(e4)), Track(tester2, EntityRemove(e3)))
      expectMsgAnyOf(Track(tester1, EntityAdd(e4)), Track(tester2, EntityRemove(e3)))

      val e5 = w1.createEntity()
      expectMsg(Track(tester1, EntityAdd(e5)))
    }
  }
}

object TrackerSpec {
  case class SomeComp1(s: String)  extends Attribute
  case class SomeComp2(i: Int)     extends Attribute
  case class SomeComp3(b: Boolean) extends Attribute
}