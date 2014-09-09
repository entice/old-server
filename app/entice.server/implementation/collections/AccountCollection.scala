/**
 * For copyright information see the LICENSE document.
 * Adapted from: https://gist.github.com/DeLongey/3757237
 */

package entice.server.implementation.collections

import entice.server._

import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext


trait AccountCollection extends Collection { self: Core =>

  case class Account(
    email: String,
    password: String,
    id: ObjectID = ObjectID()) extends DataAccessType

  implicit val accountFormat = Json.format[Account]

  lazy val accounts = AccountCollection()

  case class AccountCollection() extends CollectionLike[Account]("accounts") {
    def findByEmail(email: String) = findByQuery(Json.obj("email" -> email)).map { _.headOption }
  }
}
