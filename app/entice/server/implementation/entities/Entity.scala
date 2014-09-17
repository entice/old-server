/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.entities

import entice.server._
import entice.server.implementation.utils.Handle
import entice.server.implementation.attributes.HasAttributes
import entice.server.implementation.behaviours.HasBehaviours

import play.api.libs.json._
import play.api.libs.functional.syntax._


/** General entity contract. No tracking involved at this level */
trait Entity
    extends Handle
    with HasAttributes
    with HasBehaviours {
  def world: World#WorldLike
}

object Entity {
  def tryGetNullEntity(handleId: Handle.ID): Option[Entity] = {
    if (!Handle.exists(handleId)) { None }
    else {
      Some(new Entity {
        override lazy val id = handleId
        val world = null
        val attr = null
        val behav = null
      })
    }
  }

  object entityWrites extends Writes[Entity] {
    def writes(entity: Entity) = Json.obj("id" -> entity.id)
  }

  object entityReads extends Reads[Entity] {
    def reads(json: JsValue): JsResult[Entity] = {
      (for {
        id <- (json \ "id").asOpt[Int]
        et <- tryGetNullEntity(id)
      } yield et) match {
        case Some(et) => JsSuccess[Entity](et)
        case None     => JsError("Json to Entity: No id present or none registered")
      }
    }
  }

  implicit val entityFormat: Format[Entity] = Format[Entity](entityReads, entityWrites)
  implicit val entityListFormat: Format[List[Entity]] = implicitly[Format[List[Entity]]]
  implicit val entitySetFormat: Format[Set[Entity]] = implicitly[Format[Set[Entity]]]
}
