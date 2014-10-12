/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server.{Security, Attributes}
import play.api.libs.json._
import play.api.mvc._


case class CharacterView(name: String, appearance: Attributes#Appearance)
case class CharacterViews(chars: List[CharacterView])


/** Controller for the static website crap */
trait LobbyController extends Controller { self: Security with Attributes =>

  // Hint: this is just necessary because of some import bug
  implicit val lobbyAppearanceFormat: Format[Attributes#Appearance] = implicitly[Format[Attributes#Appearance]]

  implicit val charViewFormat = Json.format[CharacterView]
  implicit val charViewsFormat = Json.format[CharacterViews]

  def webLobby = Action { implicit request =>
    authorize match {
      case None => Forbidden
      case Some(client) => Ok(views.html.web.lobby((client().chars.map { case (n, a) => CharacterView(n, a)}).toList))
    }
  }

  def apiLobby = Action { implicit request =>
    authorize match {
      case None => Forbidden
      case Some(client) => Ok(Json.toJson(CharacterViews((client().chars.map { case (n, a) => CharacterView(n, a)}).toList)))
    }
  }
}
