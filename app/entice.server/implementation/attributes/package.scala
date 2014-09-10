/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation

import entice.server._
import entice.server.implementation.entities.Entity
import entice.server.implementation.utils.Coord2D

import play.api.libs.json._
import julienrf.variants.Variants

import scala.language.implicitConversions



package object attributes {
  import MoveState._
  import ChatChannel._
  import CharacterAnimation._
  import Map._
  import Coord2D._
  import Entity._

  implicit val animationFormat: Format[Animation] = Json.format[Animation]
  implicit val appearanceFormat: Format[Appearance] = Json.format[Appearance]
  implicit val groupStateFormat: Format[GroupState] = Json.format[GroupState]
  implicit val movementFormat: Format[Movement] = Json.format[Movement]
  implicit val nameFormat: Format[Name] = Json.format[Name]
  implicit val positionFormat: Format[Position] = Json.format[Position]

  implicit val format: Format[NetAttribute] = Variants.format[NetAttribute]("type")
}
