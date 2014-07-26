/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import events._
import util._
import behaviours._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }


/**
 * Provides actorsystem, eventbus and tracker for its entities.
 */
class World(
    val actorSystem: ActorSystem,
    val eventBus: EventBus = new EventBus,
    val tracker: Tracker = new Tracker{}) {

  private implicit val watcher: ActorRef = actorSystem.actorOf(Props(new EntityWatcher()))
  eventBus.sub[AttributeAdd]
  eventBus.sub[AttributeRemove]

  private def track[T <: Update : Named](update: T) = eventBus.pub(update)


  protected var entities: Set[Entity] = Set()
  protected def behaviours: List[BehaviourFactory[_]] =
    TrackingFactory ::
    Nil


  /** Checks if this world has a certain entity */
  def contains(entity: Entity) = entities.contains(entity)


  /** Creates an entirely new entity. Optionally with default attributes */
  def createEntity(attr: Option[ReactiveTypeMap[Attribute]] = None): Entity = {
    // create it
    val newEntity = EntityImpl(this, attr)
    // add all necessary behaviours
    behaviours.foreach { _.applyTo(newEntity) }
    // add it
    entities = entities + newEntity
    // track it :) - done
    track(EntityAdd(newEntity))
    newEntity
  }


  /** Transfers an entity from one world to another */
  def transferEntity(entity: Entity): Entity = {
    if (!entities.contains(entity)) {
      // Note: Behaviours are taken care of
      entity.world.removeEntity(entity)
      createEntity(Some(entity.attr))
    }
    else entity
  }


  /** Remove an entity from this world entirely */
  def removeEntity(entity: Entity) {
    if (entities.contains(entity)) {
      // strip it from functionality
      behaviours.foreach { _.removeFrom(entity) }
      // remove it
      entities = entities - entity
      // do not forget to dispose of the entity handle:
      entity.disposeHandle()
      // track it :) - done
      track(EntityRemove(entity))
      tracker.removeEntity(entity)
    }
  }


  /** General purpose world-event watcher for the entities of this world */
  case class EntityWatcher() extends Actor {
    def receive = {
      case Evt(a: AttributeAdd)    => behaviours.foreach { _.applyTo(a.entity) }
      case Evt(a: AttributeRemove) => behaviours.foreach { _.removeFrom(a.entity) }
    }
  }
}
