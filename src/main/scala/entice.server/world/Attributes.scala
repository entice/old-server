/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import entice.protocol._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection._


trait Attribute extends mutable.Cloneable[Attribute] with TrackingOptions


trait NoPropagation extends TrackingOptions { override def notPropagated = true }
trait NoVisibility  extends TrackingOptions { override def notVisible = true }


/** Displayed name, if any */
case class Name(name: String = "John Wayne") extends Attribute

/** Physical position in map coordinates */
case class Position(pos: Coord2D = Coord2D(0, 0)) extends Attribute

/** List of entities that this entity can see if any */
case class Vision(sees: Set[Entity] = Set()) extends Attribute with NoPropagation

/** Present if this entity can perform animations */
case class Animation(id: Animations.AniVal = Animations.None) extends Attribute

/** Present if this entity can be part of a group */
case class Group(group: Entity) extends Attribute with NoPropagation

/** A direction of movement and a state for if the entity is moving or not */
case class Movement(
    goal: Coord2D = Coord2D(1, 1),
    state: MoveState.Value = MoveState.NotMoving) extends Attribute

/** The state of a group entity */
case class GroupState(
    members: List[Entity] = Nil,
    invited: List[Entity] = Nil,
    joinRequests: List[Entity] = Nil) extends Attribute

/** The appearance of a player */
case class Appearance(
    profession: Int = 1,
    campaign: Int = 0,
    sex: Int = 1,
    height: Int = 0,
    skinColor: Int = 3,
    hairColor: Int = 0,
    hairstyle: Int = 7,
    face: Int = 31) extends Attribute