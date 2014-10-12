/**
 * For copyright information see the LICENSE document.
 */

package entice.server.models

import entice.server.Attributes
import entice.server.utils.{Collection, DataAccessType, ObjectID}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._

import scala.concurrent.Future


trait Characters { self: Attributes =>
  import entice.server.utils.ObjectID._
  import self._

  // Hint: this is just necessary because of some import bug
  implicit val charactersAppearanceFormat: Format[Attributes#Appearance] = implicitly[Format[Attributes#Appearance]]

  case class Character(
      accountId: ObjectID,
      name: String,
      appearance: Attributes#Appearance,
      id: ObjectID = ObjectID()) extends DataAccessType {
    def getName() = Name(name)
  }

  implicit val characterFormat = Json.format[Character]

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


