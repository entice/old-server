/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.attributes._
import entice.server.enums._
import entice.server.handles._
import entice.server.utils._


/** Manages entities + provides local eventbus. */
trait Worlds extends Lifecycle {

  type World <: WorldLike
  def World(name: String, map: WorldMap.WorldMapVal): World

  def lobby = World("Lobby", WorldMap.Lobby)
  def allWorlds: List[World] = lobby :: Nil

  override def onStart() {
    super.onStart()
    allWorlds.foreach(_.onStart())
  }

  override def onStop() {
    allWorlds.foreach(_.onStop())
    super.onStop()
  }

  /** Convenience access to the worlds */
  object worlds {
    def get(name: String): Option[World] = allWorlds.find { w => w.name.equalsIgnoreCase(name) }
    def worldOf(entity: EntityHandle): Option[World] = allWorlds.find { w => w.contains(entity) }
  }

  trait WorldLike extends Lifecycle { self: World =>
    def name: String
    def map: WorldMap.WorldMapVal
    val eventBus: EventBus = new EventBus()

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

