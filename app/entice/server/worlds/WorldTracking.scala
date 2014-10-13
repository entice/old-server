/**
 * For copyright information see the LICENSE document.
 */

package entice.server.worlds

import entice.server._
import entice.server.attributes._
import entice.server.handles.Entities
import entice.server.macros.Named
import entice.server.utils.ReactiveTypeMap


/** World activity tracking */
trait WorldTracking extends Worlds { self: Tracker with Entities with WorldEvents =>

  import entities.EntityHandle

  trait WorldTracker extends WorldLike { self: World =>

    private def track[T <: Update : Named](update: T) { eventBus.pubAnon(update) }

    abstract override def createEntity(attr: Option[ReactiveTypeMap[Attribute]] = None): EntityHandle = {
      val entity = super.createEntity(attr)
      track(EntityAdd(entity))
      entity
    }

    abstract override def removeEntity(entity: EntityHandle) {
      super.removeEntity(entity)
      if (contains(entity)) {
        track(EntityRemove(entity))
        tracker.removeEntity(entity)
      }
    }
  }
}
