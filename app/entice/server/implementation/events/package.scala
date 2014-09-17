/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation

import entice.server._
import entice.server.implementation.attributes._
import entice.server.implementation.entities.Entity
import entice.server.implementation.utils.Coord2D

import play.api.libs.json._
import julienrf.variants.Variants

import scala.language.implicitConversions



package object events {
  import MoveState._
  import ChatChannel._
  import CharacterAnimation._
  import WorldMap._
  import Coord2D._
  import Entity._

  implicit val format: Format[WorldEvent] = Variants.format[WorldEvent]("type")
}
