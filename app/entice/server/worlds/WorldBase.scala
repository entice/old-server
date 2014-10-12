/**
 * For copyright information see the LICENSE document.
 */

package entice.server.worlds

import entice.server._
import entice.server.handles.Entities
import entice.server.utils.{EventBus, ReactiveTypeMap}


/**
 * A world implementation based on an entity set.
 * Automatically assigns and removes attributes on entity CRUD.
 */
trait WorldBase extends Worlds {
  self: Entities
    with Attributes
    with Behaviours =>

  import entities.{Entity, EntityHandle}

  trait WorldImpl extends WorldLike { self: World =>

    private var entities: Set[EntityHandle] = Set()

    def contains(entity: EntityHandle) = entities.contains(entity)

    def createEntity(attr: Option[ReactiveTypeMap[Attribute]] = None): EntityHandle = {
      val entity = Entity(this, attr).createHandle() // create entity and its handle
      entities = entities + (entity)                 // add it
      behaviours.foreach { _.applyTo(entity) }       // add all necessary behaviours
      entity
    }

    def removeEntity(entity: EntityHandle) {
      if (contains(entity)) {
        entity.invalidate                            // make handle invalid
        behaviours.foreach { _.removeFrom(entity) }  // strip it from functionality
        entities = entities - entity                 // remove entity from world
      }
    }

    def transferEntity(entity: EntityHandle): EntityHandle = {
      if (!contains(entity)) {
        val tmpEntity = entity()                     // Danger! storage of ref only temporarily
        tmpEntity.world.removeEntity(entity)         // Note: Behaviours are taken care of
        createEntity(Some(tmpEntity.attr))
      }
      else entity
    }
  }
}
