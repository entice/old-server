/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol._


sealed trait LS2GS extends Message
case class AddPlayer(uuid: UUID, key: Long) extends LS2GS

sealed trait GS2LS extends Message
case class WaitingForPlayer(uuid: UUID)     extends GS2LS
case class CannotAddPlayer(uuid: UUID)      extends GS2LS
case class PlayerConnected(uuid: UUID)      extends GS2LS