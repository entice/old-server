/**
 * For copyright information see the LICENSE document.
 */

package entice.server.models

import entice.server.attributes._
import entice.server.enums._
import entice.server.utils.{Collection, DataAccessType, ObjectID}
import entice.server.utils.ObjectID._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._

import scala.concurrent.Future


trait Characters {

  case class Character(
      accountId: ObjectID,
      name: String,
      appearance: Appearance,
      id: ObjectID = ObjectID()) extends DataAccessType {
    def getName() = { Name(name) }
  }
  object Character {
    implicit val appearFormat: Format[Appearance] = Json.format[Appearance]
    implicit val characterFormat: Format[Character] = Json.format[Character]
  }
  import Character._

  object characters extends Collection[Character]("characters") {
    def findByAccount(accountId: ObjectID) = findByQuery(Json.obj("accountId" -> accountId))
    def findByName(name: String)           = findByQuery(Json.obj("name" -> name)).map { _.headOption }

    def deleteByName(name: String)         = {
      findByName(name).flatMap { _ match {
        case Some(char) => delete(char.id)
        case None       => Future.successful(false)
      }}
    }

    def createOrUpdateByName(name: String, char: Character) = {
      createOrUpdate(Json.obj("name" -> name), char)
    }
  }
}


