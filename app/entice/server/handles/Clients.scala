/**
 * For copyright information see the LICENSE document.
 */

package entice.server.handles

import java.util.UUID

import akka.actor.ActorRef
import entice.server.attributes._
import entice.server.utils.MultiKeyMap
import entice.server.{Worlds, Handles}
import entice.server.models.Accounts
import play.api.libs.json._

import scala.util.Try


/** The 'dumb' handle class */
case class ClientHandle private[handles] (id: UUID)
object ClientHandle {
  implicit val clientFormat: Format[ClientHandle] = Json.format[ClientHandle]
}


/** Import clients for great good! */
trait Clients extends Handles { self: Accounts with Worlds =>

  object clients extends HandleModule {
    type Id = UUID
    type Data = Client
    type Handle = ClientHandle with HandleLike

    type Email = String
    type CharName = String

    def Handle(id: UUID): Handle = new ClientHandle(id) with HandleLike
    implicit def enrich(handle: ClientHandle): ClientHandle with HandleLike = {
      registry.retrieve(handle.id) match {
        case Some(h) => h
        case None    => throw HandleInvalidException()
      }
    }

    def generateId(): Id = UUID.randomUUID()


    /** Enables storage for multiple keys to client handle association */
    object lookup extends MultiKeyMap[(Email, EntityHandle), ClientHandle]


    /** Client data storage */
    case class Client(
      account: Account,
      entity: EntityHandle = lobby.createEntity(),
      chars: Map[CharName, Appearance] = Map(),
      state: ClientState = Idle()) extends DataLike


    /** State-pattern for a client */
    trait ClientState {
      val lastChange: Long = System.currentTimeMillis() // timestamp of last state change
      def session: ActorRef   = { throw new IllegalStateException("No session for this client defined.") } // net session if playing
      def world: Worlds#World = { throw new IllegalStateException("No world for this client defined.") } // world if playing
      def chara: CharName     = { throw new IllegalStateException("No character for this client selected.") } // character if playing
    }
    case class Idle() extends ClientState // TODO timeout after x? seconds
    case class LoadingMap(
      override val session: ActorRef,
      override val world: Worlds#World,
      override val chara: CharName) extends ClientState
    case class Playing(
      override val session: ActorRef,
      override val world: Worlds#World,
      override val chara: CharName) extends ClientState
  }
}
