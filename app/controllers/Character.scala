/**
 * For copyright information see the LICENSE document.
 */

package controllers

import models.{ Character => DBCharacter }

import entice.server._
import entice.server.implementation.attributes._

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.util.Try


/** Controller for the static website crap */
object Character extends EnticeController {

  sealed trait CharacterMessage
  case class CharacterCreateForm(
      charName: String,
      campaign: String,
      profession: String,
      sex: Int,
      height: Int,
      skinColor: Int,
      hairColor: Int,
      hairstyle: Int,
      face: Int) extends CharacterMessage {
    def getAppearance() = Appearance(
      CharacterProfession.withProfessionName(profession).number,
      CharacterCampaign.withCampaignName(campaign).number,
      sex, height, skinColor, hairColor, hairstyle, face)
  }

   def validate(charName: String, campaign: String, profession: String, sex: Int, height: Int, skinColor: Int, hairColor: Int, hairstyle: Int, face: Int) = {
    (for {
      camp <- Try(CharacterCampaign.withCampaignName(campaign))
      prof <- Try(CharacterProfession.withProfessionName(profession))
    } yield {
      CharacterCreateForm(charName, campaign, profession, sex, height, skinColor, hairColor, hairstyle, face)
    }).toOption
  }

  val charCreateForm = Form(mapping(
    "charName" -> text(minLength = 4, maxLength = 16),
    "campaign" -> text,
    "profession" -> text, // theoretical 5 for +eotn +bmp
    "sex" -> default(number(min = 0, max = 1), 1),
    "height" -> default(number(min = 0, max = 15), 0), // 0 tall, 15 small
    "skinColor" -> default(number(min = 0, max = 21), 3),
    "hairColor" -> default(number(min = 0, max = 29), 0),
    "hairstyle" -> default(number(min = 0, max = 31), 7),
    "face" -> default(number(min = 0, max = 30), 30)
  )(CharacterCreateForm.apply)(CharacterCreateForm.unapply) verifying("Failed form constraints!", fields => fields match {
    case c => validate(c.charName, c.campaign, c.profession, c.sex, c.height, c.skinColor, c.hairColor, c.hairstyle, c.face).isDefined
  }))

  def charGet(action: String, name: String) = Action { implicit request =>
    authorize match {
      case NotAuthorized            => replyUnauthorized
      case PlayerContext(_, client) =>
        action match {
          case "create" => Ok(views.html.web.character.update(action, "", charCreateForm))
          case "delete" if (client.chars.contains(name)) => Ok(views.html.web.character.delete(name))
          case "edit"   if (client.chars.contains(name)) =>
            val char = client.chars(name)
            val jsonChar = Json.toJson(char).as[JsObject] - "campaign" - "profession" ++ Json.obj(
              "campaign" -> CharacterCampaign(char.campaign).toString,
              "profession" -> CharacterProfession(char.profession).toString,
              "charName" -> name)
            Ok(views.html.web.character.update(action, name, charCreateForm.bind(jsonChar)))
          case _ => Redirect(routes.Lobby.webLobby()).flashing("message" -> "Action or character name unkown.")
        }
      case _ => Ok(views.html.web.lobby(Nil)).flashing("message" -> "Unknown authorization status")
    }
  }


  def apiCharPost(action: String, name: String) = Action.async { implicit request =>
    authorize match {
      case NotAuthorized            => Future.successful(replyUnauthorized)
      case PlayerContext(_, client) =>
      action match {
        case "delete" if (client.chars.contains(name)) =>
          characters.deleteByName(name).map { x =>
            updateClient(client.copy(chars = client.chars - name))
            Redirect(routes.Lobby.webLobby()).flashing("message" -> "Character deleted.")
          }

        // for create or edit we need the form data
        case a if (a == "create" ||
                  (a == "edit" && client.chars.contains(name))) =>
          charCreateForm.bindFromRequest.fold(
            formWithErrors => { Future.successful(BadRequest(views.html.web.character.update(action, name, formWithErrors))) },
            charData => {
              val dbCharacter = DBCharacter(client.account.id, charData.charName, charData.getAppearance)
              characters.createOrUpdateByName(if (name == "") charData.charName else name, dbCharacter).map { x =>
                updateClient(client.copy(chars = (client.chars - name) + (charData.charName -> charData.getAppearance)))
                Redirect(routes.Lobby.webLobby()).flashing("message" -> s"""Character ${action.stripSuffix("e")}ed""")
              }
            })
        case _ => Future.successful(Redirect(routes.Lobby.webLobby()).flashing("message" -> "Error: Action or character name unkown."))
      }
      case _ => Future.successful(Ok(views.html.web.lobby(Nil)).flashing("message" -> "Unknown authorization status"))
    }
  }
}
