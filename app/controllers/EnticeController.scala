package controllers

import models._

import entice.server._
import entice.server.implementation.attributes._

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

import java.util.UUID


/**
 * Convenience interface to the server backend.
 */
trait EnticeController
    extends Controller
    with Accounts
    with Characters {

  sealed trait AuthorizationContext { def authToken: String }
  case object NotAuthorized extends AuthorizationContext { val authToken = "" }
  case class PlayerContext(authToken: String, client: Client) extends AuthorizationContext
  case class AdminContext(authToken: String) extends AuthorizationContext

  def replyUnauthorized = Redirect(routes.Application.index()).flashing("message" -> s"""Unauthorized. <a href="${routes.Auth.authGet()}">...log in.<a>""")

  /** Authenticate with the server */
  def authenticate(email: String, password: String): Future[Option[Client]] = {
    // construct the client from an account and its characters
    val client = accounts.findByEmail(email).flatMap { _ match {
      case Some(acc) if (acc.email == email && acc.password == password) =>
        // accumulate the chars for this account
        val emptyClient = Client(UUID.randomUUID().toString(), acc)
        characters.findByAccount(acc.id).map { chars =>
          Some(chars.foldLeft(emptyClient) { (cl, ch) =>
            cl.copy(chars = cl.chars + (ch.name -> ch.appearance))
          })
        }
      case _ => Future.successful(None)
    }}
    // now inject the new client into the server
    client.map { _.map { c =>
      server.clientRegistry.add(c)
    }}
    client
  }

  /** Remove authentication and all access rights to the server */
  def deauthenticate(authToken: String): Boolean = {
    server.clientRegistry.remove(authToken)
    // TODO trigger in eventbus
  }

  /** Authorize for a certain resource */
  def authorize(implicit request: RequestHeader): AuthorizationContext = {
    (for {
      token <- request.session.get("authToken")
      client <- server.clientRegistry.get(token)
    } yield {
      PlayerContext(token, client)
    }) match {
      case Some(ctx) => ctx
      case None      => NotAuthorized
    }
  }


  def updateClient(client: Client) = server.clientRegistry.update(client)

  private val server = entice.server.Global
}
