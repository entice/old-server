/**
 * For copyright information see the LICENSE document.
 */

package entice.server.handles

import entice.server._
import entice.server.macros._
import entice.server.utils._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._

import scala.util.Random


trait Entities extends Handles {
  self: Worlds
    with Clients
    with Attributes
    with Behaviours
    with WorldEvents =>

  /** Defines a serializable handle for entity classes */
  object entities extends HandleModule {
    type Id = Int
    type Data = Entity
    type Handle = EntityHandle

    def Handle(id: Int): Handle = new EntityHandle(id)
    case class EntityHandle(id: Int) extends HandleLike

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
        world.eventBus.pub(update)
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


    // Serialization follows...

    private def tryGetHandle(id: Int): Option[EntityHandle] = registry.retrieve(id)

    object entityWrites extends Writes[EntityHandle] {
      def writes(entity: EntityHandle) = Json.obj("id" -> entity.id)
    }

    object entityReads extends Reads[EntityHandle] {
      def reads(json: JsValue): JsResult[EntityHandle] = {
        (for {
          id <- (json \ "id").asOpt[Int]
          eh <- tryGetHandle(id)
        } yield eh) match {
          case Some(eh) => JsSuccess[EntityHandle](eh)
          case None     => JsError("Json to Entity: No id present or none registered")
        }
      }
    }

    implicit val entityFormat: Format[EntityHandle] = Format[EntityHandle](entityReads, entityWrites)
  }
}
