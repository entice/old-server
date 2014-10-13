/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server.Security
import play.api.mvc._

/** Controller for the static website crap */
trait ClientController extends Controller { self: Security =>

  object clientControl {

    def clientGet(chara: String, map: String) = Action { implicit request =>
      authorize match {
        case None => Forbidden
        case Some(client) => Ok(views.html.web.client(chara, map))
      }
    }
  }
}
