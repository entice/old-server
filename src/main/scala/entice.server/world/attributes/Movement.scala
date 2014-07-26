/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world
package attributes

import entice.protocol._


/** A direction of movement and a state for if the entity is moving or not */
case class Movement(
    goal: Coord2D = Coord2D(1, 1),
    state: MoveState.Value = MoveState.NotMoving) extends Attribute