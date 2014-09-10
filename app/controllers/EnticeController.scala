package controllers

import entice.server._

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext


/** Controller for the static website crap */
trait EnticeController extends Controller {

  sealed trait AuthorizationContext { def authToken: String }
  case object NotAuthorized extends AuthorizationContext { val authToken = "" }
  case class PlayerContext(authToken: String, client: Client) extends AuthorizationContext
  case class AdminContext(authToken: String) extends AuthorizationContext

  def replyUnauthorized = Redirect(routes.Application.index()).flashing("message" -> """Unauthorized. <a href="/login">...log in.<a>""")

  def authorize(implicit request: RequestHeader): AuthorizationContext = {
    (for {
      token <- request.session.get("authToken")
      client <- Global.authorize(token)
    } yield {
      PlayerContext(token, client)
    }) match {
      case Some(ctx) => ctx
      case None      => NotAuthorized
    }
  }
}
