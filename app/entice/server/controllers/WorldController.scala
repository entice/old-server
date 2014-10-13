/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import akka.actor._
import entice.server.macros._
import entice.server.handles.Clients
import entice.server.utils.EventBus
import entice.server.{WorldEvents, Worlds, Security}
import play.api._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util._
import scala.{Left, Right}


/** Controller for the static website crap */
trait WorldController extends Controller {
  self: Security
    with Worlds
    with Clients
    with WorldEvents =>

  import clients.Idle
  import clients.ClientHandle

  import play.api.Play.current

  object worldControl {

    def worldGet(chara: String, map: String) = WebSocket.tryAcceptWithActor[String, JsValue] { implicit request =>
      Future.successful(authorize match {
        case Some(client)
            if (client().state.isInstanceOf[Idle] // client needs to be idle in lobby or elsewhere
            &&  client().chars.contains(chara)    // client needs to own the character
            &&  worlds.get(map).isDefined) =>     // world needs to exist TODO check if character can access it
          Logger.info("Client is starting to load world: " + map)
          Right(WorldSession.props(client, chara, worlds.get(map).get.eventBus))

        case _ => Left(Forbidden)
      })
    }
  }


  object WorldSession {
    def props(client: ClientHandle, chara: String, eventBus: EventBus)(net: ActorRef) =
      Props(new WorldSession(client, chara, eventBus, net))
  }


  /** Simple Session implementation, comes with Ping/Pong enabled. */
  case class WorldSession(
      client: ClientHandle,
      chara: String,
      eventBus: EventBus,
      net: ActorRef) extends Actor with ActorLogging {

    import context._

    // Lifecycle hooks
    override def preStart() = { eventBus.pub(PlayerJoin(client, chara)) }
    override def postStop() = { eventBus.pub(PlayerQuit(client)) }


    // Ping / Pong
    val interval = Play.current.configuration.getInt("websocket.ping-interval").getOrElse(10000)
    var lastTimestamp: Long = System.currentTimeMillis()
    var lastRoundTrip: Long = 0 // in ms

    def updateTimestamp()     { lastTimestamp = System.currentTimeMillis() }
    def updateRoundTripTime() { lastRoundTrip = System.currentTimeMillis() - lastTimestamp }

    context.system.scheduler.schedule(
      Duration(interval, MILLISECONDS),
      Duration(interval, MILLISECONDS)) {
      updateTimestamp()
      self ! Ping(lastRoundTrip.toInt)
    }


    // Main message receive
    def receive = {
      case got: String     => tryPublish(got)
      case put: WorldEvent => trySend(put)
      case other           => Logger.warn("Unhandled: " + other)
    }


    /** Sends a completely deserialized and checked message to the event-bus */
    def tryPublish(in: String) = {
      Try(Json.parse(in)) match {
        case Failure(ex) => Logger.error(s"JSON parse error in:\n${in}\nException: ${ex}")
        case Success(js) =>
          Json.fromJson[WorldEvent](js).fold(
            { ex => Logger.error(s"JSON deserialize error in:\n${Json.prettyPrint(js)}\nException: ${ex}") },
            {
              case e@ Ping(delay)       => Logger.debug(s"Got Ping: RTT ${delay} ms"); self ! Pong
              case e@ Pong              => updateRoundTripTime(); Logger.debug(s"Got Pong: RTT ${lastRoundTrip} ms")
              case e: ChatMessage       => Logger.debug("Got: " + e); eventBus.pub(e)
              case e: IngameCommand     => Logger.debug("Got: " + e); eventBus.pub(e)
              case e: MoveRequest       => Logger.debug("Got: " + e); eventBus.pub(e)
              case e: GroupMergeRequest => Logger.debug("Got: " + e); eventBus.pub(e)
              case e: GroupKickRequest  => Logger.debug("Got: " + e); eventBus.pub(e)
              case other                => Logger.warn("Unregistered message from client: " + other)
            }
          )
      }
    }

    /** Sends a completely serialized message to the client */
    def trySend(out: WorldEvent) = {
      Logger.debug("Put: " + out)
      net ! Json.toJson(out)
    }
  }
}
