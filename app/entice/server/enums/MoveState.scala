/**
 * For copyright information see the LICENSE document.
 */

package entice.server.enums

import play.api.libs.json.Format


/**
 * State of a moving entity
 */
object MoveState extends Enumeration {

  type MoveState = Value

  val NotMoving               = Value("notMoving")
  val Moving                  = Value("moving")

  implicit def enumFormat: Format[MoveState] = EnumUtils.enumFormat(MoveState)
}
