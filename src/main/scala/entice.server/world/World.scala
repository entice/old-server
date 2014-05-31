/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import events.EventBus


trait World {
  private val eventBus = new EventBus();

  def subscribe() {}
}