/**
 * For copyright information see the LICENSE document.
 */

package models

import entice.server.implementation.attributes._

import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future


case class Character(
    accountId: ObjectID,
    name: String,
    appearance: Appearance,
    id: ObjectID = ObjectID()) extends DataAccessType {
  def getName() = Name(name)
}


/** Mix-in */
trait Characters {
  import ObjectID._
  implicit val characterFormat = Json.format[Character]

  lazy val characters = CharacterCollection()

  case class CharacterCollection() extends Collection[Character]("characters") {
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


