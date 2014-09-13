package controllers

import entice.server.implementation.attributes._

import play.api._
import play.api.mvc._
import play.api.libs.json._


/** Controller for the static website crap */
object Lobby extends EnticeController {

  sealed trait LobbyMessage // generally served
  case class CharacterView(name: String, appearance: Appearance) extends LobbyMessage
  implicit val charViewFormat = Json.format[CharacterView]

  sealed trait ApiLobbyMessage // served as JSON
  case class ApiCharacterViews(chars: List[CharacterView]) extends ApiLobbyMessage
  implicit val apiCharViewsFormat = Json.format[ApiCharacterViews]

  def webLobby = Action { implicit request =>
    authorize match {
      case NotAuthorized            => replyUnauthorized
      case PlayerContext(_, client) => Ok(views.html.web.lobby((client.chars.map { case (n, a) => CharacterView(n, a)}).toList))
      case _                        => Ok(views.html.web.lobby(Nil)).flashing("message" -> "Unkown authorization status")
    }
  }

  def apiLobby = Action { implicit request =>
    authorize match {
      case NotAuthorized            => replyUnauthorized
      case PlayerContext(_, client) => Ok(Json.toJson(ApiCharacterViews((client.chars.map { case (n, a) => CharacterView(n, a)}).toList)))
      case _                        => Ok(views.html.web.lobby(Nil)).flashing("message" -> "Unkown authorization status")
    }
  }
}
