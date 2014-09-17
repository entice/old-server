/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import models._

import entice.server.macros._
import entice.server.implementation.attributes._
import entice.server.implementation.utils._
import entice.server.implementation.entities.Entity

import akka.actor.ActorRef

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext


/**
 * Associates a session with a client object.
 */
trait ClientRegistry { self: Accounts with Characters =>

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
  }
}


/**
 * Client data storage
 */
case class Client(
    authToken: String,
    account: Account,
    chars: Map[String, Appearance] = Map(),
    session: Option[ActorRef] = None,
    entity: Option[Entity] = None,
    state: PlayState = Idle())


trait PlayState
case class Idle(entered: Long = System.currentTimeMillis()) extends PlayState // TODO timeout after x? seconds
case class LoadingMap(world: World#WorldLike)               extends PlayState
case class Playing(world: World#WorldLike)                  extends PlayState
