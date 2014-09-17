/**
 * For copyright information see the LICENSE document.
 */

package controllers

import entice.server._
import entice.server.macros._
import entice.server.implementation.events._
import entice.server.implementation.attributes._

import akka.actor._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.data.validation._

import scala.Left
import scala.Right
import scala.concurrent.Future


/** Controller for the static website crap */
object World extends EnticeController {
  import Play.current

  def world(chara: String, map: String) = WebSocket.tryAcceptWithActor[JsValue, JsValue] { implicit request =>
    Future.successful(authorize match {
      case NotAuthorized            => Left(replyUnauthorized)
      case PlayerContext(token, client)
           if (client.state.isInstanceOf[Idle] &&
               client.chars.contains(chara) &&
               getWorld(map).isDefined) =>
        Logger.info("Client is starting to load world: " + map)
        val world = getWorld(map).get
        updateClient(client.copy(state = LoadingMap(world)))
        Right(WorldSession.props(token, world.eventBus))
      case _                        => Left(Ok(views.html.web.lobby(Nil)).flashing("message" -> "Unknown authorization status, or unkown world."))
    })
  }
}


case class WorldSession(authToken: String, eventBus: EventBus, net: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case js: JsValue => publish(js)
    case msg: WorldEvent => log.debug("Put: " + msg); net ! Json.toJson(msg)
    case other => log.warning("Unhandled: " + other)
  }

  def publish(js: JsValue) = {
    Json.fromJson[WorldEvent](js).fold(
      { err => log.error("Unknown message received: " + err) },
      {
        case e: IngameCommand     => log.debug("Got: " + e); eventBus.pub(e)
        case e: MoveRequest       => log.debug("Got: " + e); eventBus.pub(e)
        case e: GroupMergeRequest => log.debug("Got: " + e); eventBus.pub(e)
        case e: GroupKickRequest  => log.debug("Got: " + e); eventBus.pub(e)
        case other                => log.warning("Unregistered message from client: " + other)
      }
    )
  }
}

object WorldSession {
  def props(authToken: String, eventBus: EventBus)(net: ActorRef) = Props(new WorldSession(authToken, eventBus, net))
}
