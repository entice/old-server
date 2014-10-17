/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import controllers.routes
import entice.server.attributes._
import entice.server.handles._
import entice.server.models._
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
trait CharacterController extends Controller { self: Security with Clients with Characters =>
  import clients._

  object charControl {
    // TODO refactor!


    def charGet(action: String, name: String) = Action { implicit request =>
      authorize match {
        case None => Forbidden
        case Some(client) =>
          action match {
            case "create" => Ok(views.html.web.character.update(action, "", charCreateForm))
            case "delete" if (client().chars.contains(name)) => Ok(views.html.web.character.delete(name))
            case "edit"   if (client().chars.contains(name)) => Ok(views.html.web.character.update(action, name, charCreateForm.bind(charToJson(name, client().chars(name)))))
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
              tryDeleteChar(name, client)
                .map { x => Redirect(routes.Proxy.webLobbyGet()).flashing("message" -> "Character deleted.") }

            // for create or edit we need the form data
            case a if (a == "create" ||
                      (a == "edit" && client().chars.contains(name))) =>
              charCreateForm.bindFromRequest.fold(
                formWithErrors => { Future.successful(BadRequest(views.html.web.character.update(action, name, formWithErrors))) },
                charData => {
                  tryUpdateChar(name, charData.charName, getAppearance(charData), client)
                    .map { x => Redirect(routes.Proxy.webLobbyGet()).flashing("message" -> s"""Character ${action.stripSuffix("e")}ed""") }
                })
            case _ => Future.successful(Redirect(routes.Proxy.webLobbyGet()).flashing("message" -> "Error: Action or character name unkown."))
          }
        case _ => Future.successful(Ok(views.html.web.lobby(Nil)).flashing("message" -> "Unknown authorization status"))
      }
    }


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


    private def charToJson(name: String, appear: Appearance) = {
      Json.toJson(appear).as[JsObject] - "campaign" - "profession" ++ Json.obj(
        "campaign" -> CharacterCampaign(appear.campaign).toString,
        "profession" -> CharacterProfession(appear.profession).toString,
        "charName" -> name)
    }


    private def tryDeleteChar(name: String, client: ClientHandle): Future[Boolean] = {
      characters.deleteByName(name).map { x =>
        client.update(client().copy(chars = client().chars - name))
        true
      }
    }


    private def tryUpdateChar(
        currentName: String,
        newName: String,
        appear: Appearance,
        client: ClientHandle): Future[Boolean] = {
      val dbCharacter = Character(client().account.id, newName, appear)
      val dbName = if (currentName == "") newName else currentName
      characters.createOrUpdateByName(dbName, dbCharacter).map { x =>
        client.update(client().copy(chars = (client().chars - dbName) + (newName -> appear)))
        true
      }
    }
  }
}
