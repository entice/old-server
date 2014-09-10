/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.behaviours

import entice.server.macros._
import entice.server.implementation.attributes._
import entice.server.implementation.entities._
import entice.server.implementation.events._
import entice.server.implementation.utils.ReactiveTypeMap

import akka.actor.{ ActorRef, ActorSystem, Props }

import scala.concurrent.Future
import scala.reflect.ClassTag


/** Base trait for behaviours - assumes that each one is an actor */
abstract class Behaviour(val entity: Entity) {
  def actorSystem = entity.world.actorSystem
  def eventBus    = entity.world.eventBus

  def init() { handles.foreach { _.subscribe(eventBus) } }

  /** Defines events that the behaviour wants to receive */
  protected def handles: List[Handler[_]] = List()
  protected def incoming[T : Named](implicit actor: ActorRef) = Handler[T]()
  protected case class Handler[T: Named]()(implicit actor: ActorRef) {
    def subscribe(bus: EventBus) { bus.sub[T] }
  }
}


abstract class BehaviourFactory[T <: Behaviour : Named] {

  //type T <: Behaviour
  //implicit val named: Named[T]

  /** Adds the behaviour if applicable */
  def applyTo(e: Entity): Option[T] = {
    // apply only if not yet present and all requirements met
    if (e.hasBehaviour[T] || !(requires forall { _.check(e) })) None
    else {
      val behav = creates(e)
      behav.init()
      e.addBehaviour(behav)
      Some(behav)
    }
  }

  /** Removes the behaviour if not applicable anymore */
  def removeFrom(e: Entity) { e.removeBehaviour[T] }

  /** Defines the constructor for the behaviour */
  protected def creates: ((Entity) => T)

  /** Defines necessary / unwanted attributes of the entities */
  protected def requires: List[Requirement]

  protected sealed trait Requirement { def check(e: Entity): Boolean }

  /** Defines a type that MUST be present in the entity */
  protected def        has[T <: Attribute : Named] = Has[T]()
  protected case class Has[T <: Attribute : Named]() extends Requirement {
    def check(e: Entity): Boolean = (e.has[T])
  }

  /** Defines a type that MUST NOT be present in the entity */
  protected def        hasNot[T <: Attribute : Named] = HasNot[T]()
  protected case class HasNot[T <: Attribute : Named]() extends Requirement {
    def check(e: Entity): Boolean = (!e.has[T])
  }
}


trait HasBehaviours {
  def behav: ReactiveTypeMap[Behaviour]

  def hasBehaviour   [T <: Behaviour : Named]: Boolean = behav.contains[T]
  def removeBehaviour[T <: Behaviour : Named]: this.type = { behav.remove[T]; this }
  def addBehaviour   [T <: Behaviour : Named](c: T): this.type = { behav.set(c); this }
}
