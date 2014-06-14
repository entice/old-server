/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import events._
import util._
import behaviours._

import akka.actor.{ ActorRef, ActorSystem }


class World(
    val actorSystem: ActorSystem, 
    val eventBus: EventBus = new EventBus,
    val tracker: Tracker = new Tracker {}) {

  private var entities: Set[Entity] = Set()
  private val behaviours =
    TrackingFactory ::
    Nil


  private def track[T <: Update : Named](update: T) = { 
    implicit val actor: ActorRef = null 
    eventBus.pub(update) 
  }


  def contains(entity: Entity) = entities.contains(entity)


  def createEntity(attr: Option[ReactiveTypeMap[Attribute]] = None): Entity = { 
    val newEntity = new Entity(this, attr) with EntityTracker
    entities = entities + newEntity
    track(EntityAdd(newEntity))
    newEntity
  }


  def transfer(entity: Entity): Entity = {
    if (!entities.contains(entity)) {
      // remove us from the old world
      entity.world.remove(entity)
      // inject this world
      val newEntity = entity.copy(world = this)
      // add the new entity
      entities = entities + newEntity
      track(EntityAdd(newEntity))
      newEntity
    }
    else entity
  }


  def remove(entity: Entity) { 
    if (entities.contains(entity)) {
      entities = entities - entity
      track(EntityRemove(entity))
    }
  }
}