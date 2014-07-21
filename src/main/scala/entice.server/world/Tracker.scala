/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import events._

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
  var events = Map[Entity, Queue[Update]]()

  def trackMe(entity: Entity, update: Update) {
    // update needs to be propagateable and visible to be stored
    if (!update.notPropagated && !(entity != update.entity && update.notVisible)) {
      val queue = events.get(entity).getOrElse(Queue())
      events = events + (entity -> (queue.enqueue(update)))
    }
  }
}
