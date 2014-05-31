/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import events._


trait Trackable { self: Entity =>
  def track(update: Update) = {
  }
}


object Tracker {
  def track(entity: Entity, update: Update) = {}
}