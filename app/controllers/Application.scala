package controllers

import play.api._
import play.api.mvc._

/** Controller for the static website crap */
object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }
}
