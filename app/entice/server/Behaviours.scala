/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import akka.actor.ActorRef
import entice.server.attributes._
import entice.server.handles._
import entice.server.macros._
import entice.server.utils._


trait Behaviours { self: Core with Worlds with Entities =>
  import entities._

  /** Stack them in! */
  def behaviours: List[BehaviourFactory[_]] = Nil


  /** Base trait for behaviours - assumes that each one is an actor */
  abstract class Behaviour(val entity: EntityHandle) {
    lazy val actorSystem = self.actorSystem

    def eventBus = entity().world.eventBus

    def init() {
      handles.foreach {
        _.subscribe(eventBus)
      }
    }

    /** Defines events that the behaviour wants to receive */
    protected def handles: List[Handler[_]] = List()

    protected def incoming[T](implicit named: Named[T], actor: ActorRef) = Handler[T]()

    protected case class Handler[T](implicit named: Named[T], actor: ActorRef) {
      def subscribe(bus: EventBus) {
        bus.sub[T]
      }
    }

  }


  /** Mix-in for behaviour management functionality */
  trait HasBehaviours {
    def behav: ReactiveTypeMap[Behaviours#Behaviour]

    def hasBehaviour   [T <: Behaviours#Behaviour : Named]: Boolean = behav.contains[T]
    def removeBehaviour[T <: Behaviours#Behaviour : Named]: this.type = { behav.remove[T]; this }
    def addBehaviour   [T <: Behaviours#Behaviour : Named](c: T): this.type = { behav.set(c); this }
  }


  abstract class BehaviourFactory[T <: Behaviour : Named] {

    /** Adds the behaviour if applicable */
    def applyTo(entity: EntityHandle): Option[T] = {
      // apply only if not yet present and all requirements met
      if (entity().hasBehaviour[T] || !(requires forall {
        _.check(entity)
      })) None
      else {
        val behav = creates(entity)
        behav.init()
        entity().addBehaviour(behav)
        Some(behav)
      }
    }

    /** Removes the behaviour if not applicable anymore */
    def removeFrom(entity: EntityHandle) {
      entity().removeBehaviour[T]
    }

    /** Defines the constructor for the behaviour */
    protected def creates: ((EntityHandle) => T)

    /** Defines necessary / unwanted attributes of the entities */
    protected def requires: List[Requirement]

    protected sealed trait Requirement {
      def check(entity: EntityHandle): Boolean
    }

    /** Defines a type that MUST be present in the entity */
    protected def has[T <: Attribute : Named] = Has[T]()

    protected case class Has[T <: Attribute : Named]() extends Requirement {
      def check(entity: EntityHandle): Boolean = (entity().has[T])
    }

    /** Defines a type that MUST NOT be present in the entity */
    protected def hasNot[T <: Attribute : Named] = HasNot[T]()

    protected case class HasNot[T <: Attribute : Named]() extends Requirement {
      def check(entity: EntityHandle): Boolean = (!entity().has[T])
    }
  }
}
