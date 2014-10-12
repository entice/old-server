/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import play.api.libs.json._


/** Coord format - can be interpreted as point or vector, see context */
case class Coord2D(x: Float, y: Float)
object Coord2D{
  implicit val coord2DFormat = Json.format[Coord2D]
}
