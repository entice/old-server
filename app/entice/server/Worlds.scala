/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.handles.Entities
import entice.server.utils._


/** Manages entities + provides local eventbus. */
trait Worlds { self: Entities with Attributes =>

  import entities.EntityHandle

  type World <: WorldLike
  def World(name: String, eventBus: EventBus = new EventBus()): World

  def lobby = World("Lobby")
  def allWorlds: List[World] = lobby :: Nil

  /** Convenience access to the worlds */
  object worlds {
    def get(name: String): Option[World] = allWorlds.find { w => w.name.equalsIgnoreCase(name) }
    def worldOf(entity: EntityHandle): Option[World] = allWorlds.find { w => w.contains(entity) }
  }

  trait WorldLike { self: World =>
    def name: String
    def eventBus: EventBus

    /** Checks if the given entity resides in this world */
    def contains(entity: EntityHandle) : Boolean
    /** Creates an entirely new entity. Optionally with default attributes */
    def createEntity(attr: Option[ReactiveTypeMap[Attribute]] = None): EntityHandle
    /** Remove an entity from this world entirely */
    def removeEntity(entity: EntityHandle): Unit
    /** Transfers an entity from one world to another */
    def transferEntity(entity: EntityHandle): EntityHandle
  }
}

