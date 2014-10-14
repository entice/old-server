/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import java.util.UUID

import entice.server.handles._
import entice.server.models.{Characters, Accounts}
import play.api.mvc.RequestHeader
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.util.Try


trait Security { self: Accounts with Characters with Worlds with Clients =>
  import clients._

  /** Authenticate with the server */
  def authenticate(email: String, password: String): Future[Option[ClientHandle]] = {
    accounts.findByEmail(email).flatMap {
      // if account exists and pass word is correct:
      case Some(acc) if (acc.email == email && acc.password == password) =>
        val client = Client(acc).createHandle()
        clients.lookup +((acc.email, client().entity), client)
        updateChars(client) map (Some(_))

      // if login failed:
      case _ => Future.successful(None)
    }
  }

  /** Remove authentication and all access rights to the server */
  def deauthenticate(client: ClientHandle) {
    clients.lookup - (client().account.email)
    client.invalidate()
  }

  /** Authorize for a certain resource */
  def authorize(implicit request: RequestHeader): Option[ClientHandle] = {
    for {
      token  <- request.session.get("authToken")
      uuid   <- Try(UUID.fromString(token)).toOption
      client <- clients.registry.retrieve(uuid)
    } yield { client }
  }

  private def updateChars(client: ClientHandle): Future[ClientHandle] = {
    characters.findByAccount(client().account.id) map { chars =>
      // remove all chars
      client.update(client().copy(chars = Map()))
      // add all chars from db
      chars.foldLeft(client) { (cl, ch) =>
        cl.update(cl().copy(chars = cl().chars + (ch.name -> ch.appearance)))
      }
    }
  }
}
