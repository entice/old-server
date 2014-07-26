/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import events._
import util._

import scala.collection.immutable.Queue


/** Determins propagation of changes to a value */
trait TrackingOptions {
  /** Don't send it to anybody */
  def notPropagated: Boolean = false
  /** Don't send it to nearby (observing) entities */
  def notVisible: Boolean = false || notPropagated
}


/** This keeps track of the updates that certain entities can observe */
trait Tracker {
  import scala.concurrent.ExecutionContext.Implicits.global

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

  /** Called by the world when an entity get removed. (B7c of mem issues otherwise) */
  def removeEntity(entity: Entity) { events.alter { evts => evts - entity } }

  def dump() = events.get()
}
