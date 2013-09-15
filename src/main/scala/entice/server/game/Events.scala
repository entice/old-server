/**
 * For copyright information see the LICENSE document.
 */

package entice.server.game

import entice.protocol._


sealed trait Event extends Message

// used to invoke the entity systems update method
case class Tick extends Event   // from the system
case class Flush extends Event  // from single components

case class StartMove(entity: Entity) extends Event