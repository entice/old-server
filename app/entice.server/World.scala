/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.implementation.attributes._
import entice.server.implementation.events.EventBus
import entice.server.implementation.utils._
import entice.server.implementation.entities.Entity


import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

/**
 * Provides actorsystem, eventbus and tracker for its entities.
 */
trait World { self: Core with Tracker =>

  /** Stack them */
  def worldStack: List[WorldLike] = Nil

  object worlds {
    def byName(name: String): Option[WorldLike] = worldStack.find { _.mapName == name }
  }

  trait WorldLike {
    lazy val actorSystem = self.actorSystem

    /** Should be used to identify the actual map */
    def mapName: String

    /** Local event bus*/
    def eventBus: EventBus

    /** Global or local tracker */
    def tracker: DefaultTracker

    /** Get an entity for a certain ID */
    def resolve(handle: Handle): Option[Entity]

    /** Checks if this world has a certain entity */
    def contains(entity: Entity): Boolean

    /** Creates an entirely new entity. Optionally with default attributes */
    def createEntity(attr: Option[ReactiveTypeMap[Attribute]] = None): Entity

    /** Transfers an entity from one world to another */
    def transferEntity(entity: Entity): Entity

    /** Remove an entity from this world entirely */
    def removeEntity(entity: Entity)
  }
}

