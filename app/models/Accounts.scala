/**
 * For copyright information see the LICENSE document.
 */

package models

import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext


case class Account(
    email: String,
    password: String,
    id: ObjectID = ObjectID()) extends DataAccessType


/** Mix-in */
trait Accounts {
  import ObjectID._
  implicit val accountFormat = Json.format[Account]

  lazy val accounts = AccountCollection()

  case class AccountCollection() extends Collection[Account]("accounts") {
    def findByEmail(email: String) = findByQuery(Json.obj("email" -> email)).map { _.headOption }
  }
}
