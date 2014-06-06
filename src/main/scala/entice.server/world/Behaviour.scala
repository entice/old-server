/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import Named._
import events.EventBus

import akka.actor.{ ActorRef, ActorSystem }
import scala.reflect.ClassTag


/** Base trait for behaviours - assumes that each one is an actor */
abstract class Behaviour(val entity: Entity) {
  def actorSystem = entity.world.actorSystem
  def eventBus = entity.world.eventBus
}


trait BehaviourFactory[T <: Behaviour] {
  /** Checks whether the given entity is usable by the behaviour */
  def createFor(e: Entity): Option[Behaviour] = {
    if (!(requires forall { _.check(e) })) None
    else                                   Some(createInternal(e))
  }

  protected def createInternal: ((Entity) => Behaviour)

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