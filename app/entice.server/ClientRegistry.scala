/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.macros._
import entice.server.implementation.collections._
import entice.server.implementation.attributes._
import entice.server.implementation.utils._
import entice.server.implementation.entities.Entity
import entice.server.implementation.worlds.LobbyWorld

import akka.actor.ActorRef

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import java.util.UUID


/**
 * Associates a session with a client object.
 */
trait ClientRegistry { self: AccountCollection with CharacterCollection with LobbyWorld =>

  // Convenience
  type Email = String
  type Password = String
  type AuthToken = String


  lazy val clientRegistry = DefaultClientRegistry()


  case class DefaultClientRegistry() {
    var entriesAuth:     DualMutableMap[AuthToken, Client] = DualMutableMap()
    var entriesNet:      DualMutableMap[ActorRef, Client]  = DualMutableMap()
    var entriesEntities: DualMutableMap[Entity, Client]    = DualMutableMap()

    def add(client: Client) {
      entriesAuth += (client.authToken -> client)

      // Optional values
      client.session map { sess =>
        entriesNet += (sess -> client)
      }

      client.entity map { entity =>
        entriesEntities += (entity -> client)
      }
    }

    def update(client: Client) {
      entriesAuth += (client.authToken -> client)

      // optional updates
      client.session match {
        case Some(sess) => entriesNet += (sess -> client)
        case None       => entriesNet removeRight (client)
      }

      client.entity match {
        case Some(entity) => entriesEntities += (entity -> client)
        case _            => entriesEntities removeRight (client)
      }
    }

    def remove(authToken: AuthToken): Boolean = (entriesAuth >> (authToken) map remove).isDefined
    def remove(session: ActorRef): Boolean    = (entriesNet >> (session) map remove).isDefined
    def remove(entity: Entity): Boolean       = (entriesEntities >> (entity) map remove).isDefined
    def remove(client: Client): Boolean = {
      entriesAuth     removeRight (client)
      entriesNet      removeRight (client)
      entriesEntities removeRight (client)
      true
    }

    def get(authToken: AuthToken) = entriesAuth >> (authToken)
    def get(session: ActorRef)    = entriesNet >> (session)
    def get(entity: Entity)       = entriesEntities >> (entity)

    def getAll = entriesAuth.valuesRight
    def getbyEmail(email: Email) = getAll find {_.account.email == email}

    def authenticate(email: Email, password: Password): Future[Option[Client]] = {
      self.accounts.findByEmail(email).flatMap { _ match {
        case Some(acc) if (acc.email == email && acc.password == password) =>
          // accumulate the chars for this account
          val emptyClient = Client(UUID.randomUUID().toString(), acc)
          self.characters.findByAccount(acc.id).map { chars =>
            Some(chars.foldLeft(emptyClient) { (cl, ch) =>
              val name: Name = ch.getName
              val appear: Appearance = ch.appearance
              val attrMap = new ReactiveTypeMap[Attribute]().add(name).add(appear)
              cl.copy(chars = cl.chars + (lobby.createEntity(Some(attrMap)) -> ((name, appear))))
            }).map { client => add(client); client}
          }
        case _ => Future.successful(None)
      }}
    }
  }
}


/**
   * Client data storage
   */
  case class Client(
      authToken: String,
      account: AccountCollection#Account,
      chars: Map[Entity, (Name, Appearance)] = Map(),
      session: Option[ActorRef] = None,
      entity: Option[Entity] = None,
      state: PlayState = Idle)


  trait PlayState
  case object Idle       extends PlayState // TODO timeout after x? seconds
  case object LoadingMap extends PlayState
  case object Playing    extends PlayState
