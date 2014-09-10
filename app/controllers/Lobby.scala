package controllers

import play.api._
import play.api.mvc._

/** Controller for the static website crap */
object Lobby extends EnticeController {

  def lobby = Action { implicit request =>
    authorize match {
      case NotAuthorized => replyUnauthorized
      case _             => Ok(views.html.lobby())
    }
  }
}
