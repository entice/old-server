/**
 * For copyright information see the LICENSE document.
 */

package entice.server.handles

import entice.server._
import entice.server.attributes._
import entice.server.events._
import entice.server.macros._
import entice.server.utils._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._

import scala.util.Random


/** The 'dumb' handle class */
case class EntityHandle(id: Int)
object EntityHandle {
  implicit val entityFormat: Format[EntityHandle] = Json.format[EntityHandle]
}


/** Import entities for great good! */
trait Entities extends Handles {
  self: Worlds
    with Clients
    with Behaviours =>

  /** Defines a serializable handle for entity classes */
  object entities extends HandleModule {
    type Id = Int
    type Data = Entity
    type Handle = EntityHandle with HandleLike

    def Handle(id: Int): Handle = new EntityHandle(id) with HandleLike
    implicit def enrichEntity(handle: EntityHandle): EntityHandle with HandleLike = {
      registry.retrieve(handle.id) match {
        case Some(h) => h
        case None    => throw HandleInvalidException()
      }
    }

    def generateId() = ran.nextInt()
    private val ran = new Random()


    /** General entity contract. No tracking involved at this level */
    case class Entity(
      world: World,
      initialAttr: Option[ReactiveTypeMap[Attribute]] = None)
      extends DataLike with HasAttributes with HasBehaviours {

      // create attribute and behaviour maps
      val attr = initialAttr.getOrElse(new ReactiveTypeMap())
      val behav = new ReactiveTypeMap[Behaviours#Behaviour]()

      var handle: EntityHandle = _

      override def createHandle() = {
        handle = super.createHandle()
        handle
      }
    }


    /** Add automagical tracking to an entities 'state changes'. Needs Tracking behaviour */
    trait EntityTracker extends HasAttributes { self: Entity =>

      def track[T <: Update : Named](update: T) = {
        world.eventBus.pubAnon(update)
      }

      abstract override def add[T <: Attribute : Named](c: T) = {
        super.get[T] match {
          case Some(attr) => attr onSuccess { case a if (a != c) => track(AttributeChange(handle, a, c))}
          case None => track(AttributeAdd(handle, c))
          case _ => // do nothing if component the same
        }
        super.add(c)
      }

      abstract override def remove[T <: Attribute : Named] = {
        super.get[T] match {
          case Some(attr) => attr onSuccess { case a => track(AttributeRemove(handle, a))}
          case _ => // do nothing if component doesnt exist
        }
        super.remove[T]
      }
    }
  }
}
