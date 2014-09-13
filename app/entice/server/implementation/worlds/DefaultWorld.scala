/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.worlds

import entice.server._
import entice.server.macros._
import entice.server.implementation.attributes._
import entice.server.implementation.behaviours._
import entice.server.implementation.entities._
import entice.server.implementation.events._
import entice.server.implementation.utils._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }


/**
 * Base world implementation
 */
trait DefaultWorld extends World { self: Core with Tracker =>

  case class WorldImpl(
      val mapName: String,
      val tracker: DefaultTracker) extends WorldLike {

    val eventBus: EventBus = new EventBus()

    private implicit val watcher: ActorRef = actorSystem.actorOf(Props[EntityWatcher])
    eventBus.sub[AttributeAdd]
    eventBus.sub[AttributeRemove]

    private def track[T <: Update : Named](update: T) = eventBus.pub(update)


    protected var entities: Map[Handle.ID, Entity] = Map()
    protected def behaviours: List[BehaviourFactory[_]] =
      TrackingFactory ::
      Nil


    /** Get an entity for a certain ID */
    def resolve(handle: Handle): Option[Entity] = entities.get(handle.id)


    /** Checks if this world has a certain entity */
    def contains(entity: Entity) = entities.contains(entity.id)


    /** Creates an entirely new entity. Optionally with default attributes */
    def createEntity(attr: Option[ReactiveTypeMap[Attribute]] = None): Entity = {
      // create it
      val newEntity = WorldEntity(this, attr)
      // add all necessary behaviours
      behaviours.foreach { _.applyTo(newEntity) }
      // add it
      entities = entities + (newEntity.id -> newEntity)
      // track it :) - done
      track(EntityAdd(newEntity))
      newEntity
    }


    /** Remove an entity from this world entirely */
    def removeEntity(entity: Entity) {
      entities.get(entity.id).map { e =>
        // strip it from functionality
        behaviours.foreach { _.removeFrom(entity) }
        // remove it
        entities = entities - entity.id
        // do not forget to dispose of the entity handle:
        entity.disposeHandle()
        // track it :) - done
        track(EntityRemove(entity))
        tracker.removeEntity(entity)
      }
    }


    /** Transfers an entity from one world to another */
    def transferEntity(entity: Entity): Entity = {
      if (!entities.contains(entity.id)) {
        // Note: Behaviours are taken care of
        entity.world.removeEntity(entity)
        createEntity(Some(entity.attr))
      }
      else entity
    }


    /** General purpose world-event watcher for the entities of this world */
    class EntityWatcher extends Actor {
      def receive = {
        case Evt(a: AttributeAdd)    => behaviours.foreach { _.applyTo(a.entity) }
        case Evt(a: AttributeRemove) => behaviours.foreach { _.removeFrom(a.entity) }
      }
    }
  }
}
