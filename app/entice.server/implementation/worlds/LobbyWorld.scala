/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.worlds

import entice.server._
import entice.server.implementation.events.EventBus
import entice.server.implementation.attributes.Attribute
import entice.server.implementation.utils._
import entice.server.implementation.entities.{ Entity, LobbyEntity }


/**
 * Do nothing world. To get here, you need to login :P
 * This does not manage its entities.
 */
trait LobbyWorld extends World { self: Core with Tracker =>

  abstract override def worldStack = super.worldStack ::: lobby :: Nil

  object lobby extends WorldLike {
    def mapName = "Lobby"
    def eventBus = self.eventBus
    def tracker = self.tracker

    def resolve(handle: Handle): Option[Entity] = None
    def contains(entity: Entity) = false
    def createEntity(attr: Option[ReactiveTypeMap[Attribute]] = None): Entity = LobbyEntity(this)
    def transferEntity(entity: Entity): Entity = {
      entity.world.removeEntity(entity)
      LobbyEntity(this)
    }
    def removeEntity(entity: Entity) {}
  }
}
