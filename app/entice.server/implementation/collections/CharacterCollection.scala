/**
 * For copyright information see the LICENSE document.
 * Adapted from: https://gist.github.com/DeLongey/3757237
 */

package entice.server.implementation.collections

import entice.server._
import entice.protocol._

import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future


trait CharacterCollection extends Collection { self: Core =>

  type AccountID = ObjectID

  case class Character(
      accountId: AccountID,
      name: String,
      appearance: Appearance,
      id: ObjectID = ObjectID()) extends DataAccessType {
    def getName() = Name(name)
  }

  implicit val characterFormat = Json.format[Character]

  lazy val characters = CharacterCollection()

  case class CharacterCollection() extends CollectionLike[Character]("Characters") {
    def findByAccount(accountId: AccountID) = findByQuery(Json.obj("accountId" -> accountId))
    def findByName(name: String)            = findByQuery(Json.obj("name" -> name)).map { _.headOption }
    def deleteByName(name: String)          = {
      findByName(name).flatMap { _ match {
        case Some(char) => delete(char.id)
        case None       => Future.successful(false)
      }}
    }
  }
}
