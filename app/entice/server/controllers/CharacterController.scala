/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import controllers.routes
import entice.server.attributes._
import entice.server.models.Characters
import entice.server.Security
import entice.server.enums.{CharacterCampaign, CharacterProfession}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.util.Try


/** UI form for web service/interface */
case class CharacterCreateForm(
  charName: String,
  campaign: String,
  profession: String,
  sex: Int,
  height: Int,
  skinColor: Int,
  hairColor: Int,
  hairstyle: Int,
  face: Int)


/** Controller for the static website crap */
trait CharacterController extends Controller { self: Security with Characters =>

  object charControl {

    private def getAppearance(f: CharacterCreateForm): Appearance = {
      // TODO: why the fuck do we need to cast this crap?
      Appearance(
        CharacterProfession.withProfessionName(f.profession).number,
        CharacterCampaign.withCampaignName(f.campaign).number,
        f.sex, f.height, f.skinColor, f.hairColor, f.hairstyle, f.face)
    }


    private def validate(charName: String, campaign: String, profession: String, sex: Int, height: Int, skinColor: Int, hairColor: Int, hairstyle: Int, face: Int) = {
      (for {
        camp <- Try(CharacterCampaign.withCampaignName(campaign))
        prof <- Try(CharacterProfession.withProfessionName(profession))
      } yield {
        CharacterCreateForm(charName, campaign, profession, sex, height, skinColor, hairColor, hairstyle, face)
      }).toOption
    }

    private val charCreateForm = Form(mapping(
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
        case None => Forbidden
        case Some(client) =>
          action match {
            case "create" => Ok(views.html.web.character.update(action, "", charCreateForm))
            case "delete" if (client().chars.contains(name)) => Ok(views.html.web.character.delete(name))
            case "edit"   if (client().chars.contains(name)) =>
              val char = client().chars(name)
              val jsonChar = Json.toJson(char).as[JsObject] - "campaign" - "profession" ++ Json.obj(
                "campaign" -> CharacterCampaign(char.campaign).toString,
                "profession" -> CharacterProfession(char.profession).toString,
                "charName" -> name)
              Ok(views.html.web.character.update(action, name, charCreateForm.bind(jsonChar)))
            case _ => Redirect(routes.Proxy.webLobbyGet()).flashing("message" -> "Action or character name unkown.")
          }
        case _ => Ok(views.html.web.lobby(Nil)).flashing("message" -> "Unknown authorization status")
      }
    }


    def charPost(action: String, name: String) = Action.async { implicit request =>
      authorize match {
        case None => Future.successful(Forbidden)
        case Some(client) =>
          action match {
            case "delete" if (client().chars.contains(name)) =>
              characters.deleteByName(name).map { x =>
                client.update(client().copy(chars = client().chars - name))
                Redirect(routes.Proxy.webLobbyGet()).flashing("message" -> "Character deleted.")
              }

            // for create or edit we need the form data
            case a if (a == "create" ||
                      (a == "edit" && client().chars.contains(name))) =>
              charCreateForm.bindFromRequest.fold(
                formWithErrors => { Future.successful(BadRequest(views.html.web.character.update(action, name, formWithErrors))) },
                charData => {
                  val dbCharacter = Character(client().account.id, charData.charName, getAppearance(charData))
                  characters.createOrUpdateByName(if (name == "") charData.charName else name, dbCharacter).map { x =>
                    client.update(client().copy(chars = (client().chars - name) + (charData.charName -> getAppearance(charData))))
                    Redirect(routes.Proxy.webLobbyGet()).flashing("message" -> s"""Character ${action.stripSuffix("e")}ed""")
                  }
                })
            case _ => Future.successful(Redirect(routes.Proxy.webLobbyGet()).flashing("message" -> "Error: Action or character name unkown."))
          }
        case _ => Future.successful(Ok(views.html.web.lobby(Nil)).flashing("message" -> "Unknown authorization status"))
      }
    }
  }
}
