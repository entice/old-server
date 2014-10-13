/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server.Security
import entice.server.attributes._
import play.api.libs.json._
import play.api.mvc._


case class CharacterView(name: String, appearance: Appearance)
case class CharacterViews(chars: List[CharacterView])


/** Controller for the static website crap */
trait LobbyController extends Controller { self: Security =>

  object lobbyControl {

    implicit val appearFormat = Json.format[Appearance]
    implicit val charViewFormat = Json.format[CharacterView]
    implicit val charViewsFormat = Json.format[CharacterViews]

    def webLobbyGet = Action { implicit request =>
      authorize match {
        case None => Forbidden
        case Some(client) => Ok(views.html.web.lobby((client().chars.map { case (n, a) => CharacterView(n, a)}).toList))
      }
    }

    def apiLobbyGet = Action { implicit request =>
      authorize match {
        case None => Forbidden
        case Some(client) => Ok(Json.toJson(CharacterViews((client().chars.map { case (n, a) => CharacterView(n, a)}).toList)))
      }
    }
  }
}
