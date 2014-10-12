/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.enums.{CharacterAnimation, MoveState}
import entice.server.handles.Entities
import entice.server.macros.Named
import entice.server.utils._
import julienrf.variants.Variants
import play.api.libs.json.{Format, Json}

import scala.collection._
import scala.concurrent.Future


trait Attributes { self: Tracker with Entities =>
  import entities.EntityHandle

  /**
   * Attributes are logic-free dataholders that define the state of any entity.
   * The composition of these attributes defines the entity-type and behaviour of the entity.
   * Whenever the attributes of an entity change, the world that it is assigned to will
   * automatically un-/register behaviours for this entity.
   * All attributes are serializable, but only some will actually get serialized.
   */
  sealed trait Attribute extends mutable.Cloneable[Attribute] with TrackingOptions


  /** Mix-in to gain attribute management functionality */
  trait HasAttributes {
    def attr: ReactiveTypeMap[Attribute]

    def has[T <: Attribute : Named]: Boolean = attr.contains[T]

    /** Unsafe get. Returns the future value if possible. */
    def apply[T <: Attribute : Named]: Future[T] = attr.get[T].get

    /** Optional get. Returns an option of the future value. Always. */
    def get[T <: Attribute : Named]: Option[Future[T]] = attr.get[T]

    /** Remove an attribute from the set. */
    def -     [T <: Attribute : Named]: this.type = remove[T]
    def remove[T <: Attribute : Named]: this.type = { attr.remove[T]; this }

    /** Add or set an attribute. */
    def set   [T <: Attribute : Named](c: T): this.type = add(c)
    def +     [T <: Attribute : Named](c: T): this.type = add(c)
    def add   [T <: Attribute : Named](c: T): this.type = { attr.set(c); this }
  }


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
  case class Group(group: EntityHandle) extends Attribute with NoPropagation

  /** The state of a group entity */
  case class GroupState(
      members: List[EntityHandle] = Nil,
      invited: List[EntityHandle] = Nil,
      joinRequests: List[EntityHandle] = Nil) extends Attribute

  /** A direction of movement and a state for if the entity is moving or not */
  case class Movement(
      goal: Coord2D = Coord2D(1, 1),
      state: MoveState.Value = MoveState.NotMoving) extends Attribute

  /** Displayed name, if any */
  case class Name(name: String = "John Wayne") extends Attribute

  /** Physical position in map coordinates */
  case class Position(pos: Coord2D = Coord2D(0, 0)) extends Attribute

  /** Self reference */
  case class Self(entity: EntityHandle) extends Attribute with NoPropagation

  /** List of entities that this entity can see if any */
  case class Vision(sees: List[EntityHandle] = Nil) extends Attribute with NoPropagation


  // Serialization follows ...

  // Hint: This is not really necessary, just for ease of use for single use
  implicit val animationFormat: Format[Animation] = Json.format[Animation]
  implicit val appearanceFormat: Format[Appearance] = Json.format[Appearance]
  implicit val groupFormat: Format[Group] = Json.format[Group]
  implicit val groupStateFormat: Format[GroupState] = Json.format[GroupState]
  implicit val movementFormat: Format[Movement] = Json.format[Movement]
  implicit val nameFormat: Format[Name] = Json.format[Name]
  implicit val positionFormat: Format[Position] = Json.format[Position]
  implicit val selfFormat: Format[Self] = Json.format[Self]
  implicit val visionFormat: Format[Vision] = Json.format[Vision]

  implicit val attributeFormat: Format[Attribute] = Variants.format[Attribute]("type")
}
