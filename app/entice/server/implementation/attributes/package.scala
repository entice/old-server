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
  import CharacterAnimation._
  import WorldMap._
  import Coord2D._
  import Entity._


  // Hint: This is not really necessary, just for ease of use for single use
  implicit val animationFormat: Format[Animation] = Json.format[Animation]
  implicit val appearanceFormat: Format[Appearance] = Json.format[Appearance]
  implicit val groupFormat: Format[Group] = Json.format[Group]
  implicit val groupStateFormat: Format[GroupState] = Json.format[GroupState]
  implicit val movementFormat: Format[Movement] = Json.format[Movement]
  implicit val nameFormat: Format[Name] = Json.format[Name]
  implicit val positionFormat: Format[Position] = Json.format[Position]
  implicit val visionFormat: Format[Vision] = Json.format[Vision]

  implicit val format: Format[Attribute] = Variants.format[Attribute]("type")
}
