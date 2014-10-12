/**
 * For copyright information see the LICENSE document.
 */

package controllers

import play.api.mvc._

/** Controller for the static website crap */
object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }
}
