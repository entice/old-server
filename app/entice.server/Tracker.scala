/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.implementation.entities.Entity
import entice.server.implementation.events._
import entice.server.implementation.utils._

import scala.collection.immutable.Queue

import play.api.libs.concurrent.Execution.Implicits.defaultContext


/**
 * This keeps track of the updates that certain entities can observe
 */
trait Tracker {

  lazy val tracker = DefaultTracker()

  /** This keeps track of the updates that certain entities can observe */
  case class DefaultTracker() {
    var events = Agent[Map[Entity, Queue[Update]]](Map())

    /** Called by entity. Tracks an event that it can observe */
    def trackMe(entity: Entity, update: Update) {
      // update needs to be propagateable and visible to be stored
      if (!update.notPropagated && !(entity != update.entity && update.notVisible)) {
        events.alter { evts =>
          val queue = evts.get(entity).getOrElse(Queue())
          evts + (entity -> (queue.enqueue(update)))
        }
      }
    }

    /** Called by the world when an entity get removed. (B/c of mem issues otherwise) */
    def removeEntity(entity: Entity) { events.alter { evts => evts - entity } }

    def dump() = events.get()

    def pushToClients() = ???
  }
}

/** Determins propagation of changes to a value */
trait TrackingOptions {
  /** Don't send it to anybody */
  def notPropagated: Boolean = false
  /** Don't send it to nearby (observing) entities */
  def notVisible: Boolean = false || notPropagated
}

trait NoPropagation extends TrackingOptions { self: TrackingOptions => override def notPropagated = true }
trait NoVisibility  extends TrackingOptions { self: TrackingOptions => override def notVisible = true }

