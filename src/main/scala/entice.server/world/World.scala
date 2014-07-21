/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import events._
import util._
import behaviours._

import akka.actor.{ ActorRef, ActorSystem }


/**
 * Provides actorsystem, eventbus and tracker for its entities.
 */
class World(
    val actorSystem: ActorSystem,
    val eventBus: EventBus = new EventBus,
    val tracker: Tracker = new Tracker{}) {

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
    val newEntity = EntityImpl(this, attr)
    entities = entities + newEntity
    track(EntityAdd(newEntity))
    newEntity
  }


  def transfer(entity: Entity): Entity = {
    if (!entities.contains(entity)) {
      entity.world.remove(entity)
      createEntity(Some(entity.attr))
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
