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


  import TrackerSpec._

  "A tracking behaviour for entities" must {
    
    "receive component changes" in {
      val tracker = TestTracker()
      val w = new World(system, tracker = tracker)
      val s1 = SomeComp1("Test")
      val s2 = SomeComp2(1337)
      val s3 = SomeComp3(false)
      val e = (new Entity(w) with EntityTracker) + Vision() + s1 + s2
      TrackingFactory.createFor(e) must be(Some(Tracking(e)))

      within(10000 millis) {
        tracker.expect(e, AttributeRemove(e, s1))
        e.remove[SomeComp1]
        tracker.called must be(true)
        tracker.success must be(true)
      }
    }
  }
}

object TrackerSpec {
  case class SomeComp1(s: String)  extends Attribute
  case class SomeComp2(i: Int)     extends Attribute
  case class SomeComp3(b: Boolean) extends Attribute

  case class TestTracker() extends entice.server.world.Tracker {
    var entity: Entity = _
    var update: Update = _
    var called = false
    var success = false

    def expect(entity: Entity, upd: Update) {
      this.entity = entity
      this.update = upd
      called = false
      success = false
    }

    override def trackMe(entity: Entity, upd: Update) {
      called = true
      if (entity == this.entity && upd == this.update) {
        success = true
      }
    }
  }
}