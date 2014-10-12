/**
 * For copyright information see the LICENSE document.
 */

package entice.server.models

import entice.server.utils.{DataAccessType, ObjectID, Collection}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._


trait Accounts {

  case class Account(
      email: String,
      password: String,
      id: ObjectID = ObjectID()) extends DataAccessType

  implicit val accountFormat = Json.format[Account]

  object accounts extends Collection[Account]("accounts") {
    def findByEmail(email: String) = findByQuery(Json.obj("email" -> email)).map { _.headOption }
  }
}
