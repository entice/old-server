/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world
package attributes

import entice.protocol._


/** Physical position in map coordinates */
case class Position(pos: Coord2D = Coord2D(0, 0)) extends Attribute