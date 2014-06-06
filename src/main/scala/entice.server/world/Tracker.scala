/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import events._


/** Determins propagation of changes to a value */
trait TrackingOptions {
  /** Don't send it to anybody */
  def notPropagated: Boolean = false
  /** Don't send it to nearby (observing) entities */
  def notVisible: Boolean = false
}


class Tracker {
  def trackMe(entity: Entity, update: Update) = {}
}
