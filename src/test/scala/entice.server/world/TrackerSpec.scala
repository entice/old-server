/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import util._
import events._
import test._

import org.scalatest._

import scala.collection.immutable.Queue
import scala.concurrent.Await


class TrackerSpec extends WordSpec with MustMatchers with ReactiveTools {

  import TrackerSpec._

  trait TrackerScope {
    val tester1 = MockEntity()
    val tester2 = MockEntity()
    tester1 must not be(tester2) // case classes are checked by value not reference
    val tracker = new Tracker {}
  }

  "An entity update tracker" must {

    "track appropriate updates" in new TrackerScope {
      // calling the tracker directly
      tracker.trackMe(tester1, AttributeAdd(tester1, NormalAttr()))
      tracker.trackMe(tester2, AttributeAdd(tester1, NormalAttr()))
      // then we expect it to log our event
      whenReady(tracker.dump) { 
        _ must be(Map(
          tester1 -> Queue(AttributeAdd(tester1, NormalAttr())),
          tester2 -> Queue(AttributeAdd(tester1, NormalAttr())))) 
      }
    }

    "not track invisible updates" in new TrackerScope {
      // calling the tracker directly
      tracker.trackMe(tester1, AttributeAdd(tester1, NotVisibleAttr())) // will see
      tracker.trackMe(tester2, AttributeAdd(tester1, NotVisibleAttr())) // will not see
      // then we expect it to log our event only for tester1
      whenReady(tracker.dump) { 
        _ must be(Map(tester1 -> Queue(AttributeAdd(tester1, NotVisibleAttr())))) 
      }
    }

    "not track not-propagated updates" in new TrackerScope {
      // calling the tracker directly
      tracker.trackMe(tester1, AttributeAdd(tester1, NotPropagatedAttr())) // will not see
      tracker.trackMe(tester2, AttributeAdd(tester1, NotPropagatedAttr())) // will not see
      // then we expect it to log our event only for tester1
      whenReady(tracker.dump) { 
        _ must be(Map()) 
      }
    }
  }
}

object TrackerSpec {
  case class NormalAttr() extends Attribute
  case class NotVisibleAttr() extends Attribute with NoVisibility
  case class NotPropagatedAttr() extends Attribute with NoPropagation
}
