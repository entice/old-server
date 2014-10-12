/**
 * For copyright information see the LICENSE document.
 */

package entice.server.handles

import java.util.UUID

import akka.actor.ActorRef
import entice.server.utils.MultiKeyMap
import entice.server.{Worlds, Attributes, Handles}
import entice.server.models.Accounts
import play.api.libs.json._

import scala.util.Try


trait Clients extends Handles { self: Accounts with Worlds with Entities with Attributes =>
  import entities.EntityHandle

  object clients extends HandleModule {
    type Id = UUID
    type Data = Client
    type Handle = ClientHandle

    type Email = String
    type CharName = String

    def Handle(id: UUID): Handle = new ClientHandle(id)
    case class ClientHandle(id: UUID) extends HandleLike

    def generateId(): Id = UUID.randomUUID()


    /** Enables storage for multiple keys to client handle association */
    object lookup extends MultiKeyMap[(Email, EntityHandle), ClientHandle]


    /** Client data storage */
    case class Client(
      account: Account,
      entity: EntityHandle = lobby.createEntity(),
      chars: Map[CharName, Attributes#Appearance] = Map(),
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


    // Serialization follows...

    private def tryGetHandle(id: String): Option[ClientHandle] = {
      for {
        uuid <- Try(UUID.fromString(id)).toOption
        client <- registry.retrieve(uuid)
      } yield client
    }

    object clientWrites extends Writes[ClientHandle] {
      def writes(client: ClientHandle) = Json.obj("client" -> client.id)
    }

    object clientReads extends Reads[ClientHandle] {
      def reads(json: JsValue): JsResult[ClientHandle] = {
        (for {
          id <- (json \ "client").asOpt[String]
          eh <- tryGetHandle(id)
        } yield eh) match {
          case Some(eh) => JsSuccess[ClientHandle](eh)
          case None => JsError("Json to client: No uuid present or none registered")
        }
      }
    }

    implicit val clientFormat: Format[ClientHandle] = Format[ClientHandle](clientReads, clientWrites)
    implicit val clientListFormat: Format[List[ClientHandle]] = implicitly[Format[List[ClientHandle]]]
    implicit val clientSetFormat: Format[Set[ClientHandle]] = implicitly[Format[Set[ClientHandle]]]
  }
}
