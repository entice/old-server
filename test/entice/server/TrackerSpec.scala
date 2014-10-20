/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.attributes._
import entice.server.attributes.test._
import entice.server.events._
import entice.server.handles._
import entice.server.test._
import entice.server.utils._

import play.api._
import play.api.test._
import play.api.test.Helpers._

import scala.collection.immutable.Queue
import scala.concurrent.Await

import org.specs2.mutable._
import org.specs2.mock._
import org.specs2.specification._


class TrackerSpec extends Specification with Mockito {

  trait TrackerScope extends Scope
      with Mockito
      with Tracker {
    val tester1 = EntityHandle(123)
    val tester2 = EntityHandle(321)
    tester1 should not be(tester2) // case classes are checked by value not reference
    tester1 should be(tester1)
  }

  "An entity update tracker" should {

    "track appropriate updates" in new TrackerScope {
      // calling the tracker directly
      tracker.trackMe(tester1, AttributeAdd(tester1, NormalAttr()))
      tracker.trackMe(tester2, AttributeAdd(tester1, NormalAttr()))
      // then we expect it to log our event
      tracker.dump should ===[Map[EntityHandle, Queue[Update]]](Map(
        tester1 -> Queue(AttributeAdd(tester1, NormalAttr())),
        tester2 -> Queue(AttributeAdd(tester1, NormalAttr())))).await
    }

    "not track invisible updates" in new TrackerScope {
      // calling the tracker directly
      tracker.trackMe(tester1, AttributeAdd(tester1, NotVisibleAttr())) // will see
      tracker.trackMe(tester2, AttributeAdd(tester1, NotVisibleAttr())) // will not see
      // then we expect it to log our event only for tester1
      tracker.dump should ===[Map[EntityHandle, Queue[Update]]](Map(
        tester1 -> Queue(AttributeAdd(tester1, NotVisibleAttr())))).await
    }

    "not track not-propagated updates" in new TrackerScope {
      // calling the tracker directly
      tracker.trackMe(tester1, AttributeAdd(tester1, NotPropagatedAttr())) // will not see
      tracker.trackMe(tester2, AttributeAdd(tester1, NotPropagatedAttr())) // will not see
      // then we expect it to log our event only for tester1
      tracker.dump should be[Map[EntityHandle, Queue[Update]]](Map()).await
    }
  }
}
