/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import controllers.routes
import entice.server._
import play.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future


/** UI form for web service/interface */
case class AuthForm(email: String, password: String)


/** Authentication controller. You need to be logged in for most other functionality. */
trait AuthController extends Controller { self: Security =>

  val authForm = Form(mapping(
    "email" -> email,
    "password" -> text(minLength = 4, maxLength = 32)
  )(AuthForm.apply)(AuthForm.unapply))


  def authGet = Action { implicit request =>
    authorize match {
      case None => Ok(views.html.web.login(authForm))
      case _    => Ok(views.html.web.logout())
    }
  }

  def loginPost = Action.async { implicit request =>
    authForm.bindFromRequest.fold(
      formWithErrors => { Future.successful(BadRequest(views.html.web.login(formWithErrors))) },
      authData => {
        authenticate(authData.email, authData.password).map {
          case None => Forbidden
          case Some(client) =>
            Logger.info(s"User logged in: ${authData.email} - ${client.id}")
            Redirect(entice.server.routes.Global.webLobby())
              .flashing("message" -> "Successfully logged in.")
              .withSession {
              request.session + ("authToken" -> client.id.toString)
            }
        }
      })
  }

  def logoutPost = Action { implicit request =>
    authorize match {
      case None => Redirect(routes.Application.index()).withNewSession
      case Some(client) =>
        val email = client().account.email
        deauthenticate(client)
        Logger.info(s"User logged out: ${email} - ${client.id}")
        Redirect(routes.Application.index())
          .flashing("message" -> "Successfully logged out.")
          .withNewSession
    }
  }
}
