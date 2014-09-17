/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.attributes

import entice.server._
import entice.server.implementation.entities.Entity
import entice.server.implementation.utils._

import scala.collection._


sealed trait Attribute extends mutable.Cloneable[Attribute] with TrackingOptions


// Attributes follow...


/** Present if this entity can perform animations */
case class Animation(id: CharacterAnimation.Value = CharacterAnimation.None) extends Attribute

/** The appearance of a player */
case class Appearance(
    profession: Int = 1,
    campaign: Int = 0,
    sex: Int = 1,
    height: Int = 0,
    skinColor: Int = 3,
    hairColor: Int = 0,
    hairstyle: Int = 7,
    face: Int = 30) extends Attribute

/** Present if this entity can be part of a group */
case class Group(group: Entity) extends Attribute with NoPropagation

/** The state of a group entity */
case class GroupState(
    members: List[Entity] = Nil,
    invited: List[Entity] = Nil,
    joinRequests: List[Entity] = Nil) extends Attribute

/** A direction of movement and a state for if the entity is moving or not */
case class Movement(
    goal: Coord2D = Coord2D(1, 1),
    state: MoveState.Value = MoveState.NotMoving) extends Attribute

/** Displayed name, if any */
case class Name(name: String = "John Wayne") extends Attribute

/** Physical position in map coordinates */
case class Position(pos: Coord2D = Coord2D(0, 0)) extends Attribute

/** List of entities that this entity can see if any */
case class Vision(sees: List[Entity] = Nil) extends Attribute with NoPropagation
