/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.database._
import entice.server.world._
import entice.server.utils._
import entice.protocol._

import akka.actor.ActorRef

import scala.util._


/**
 * Client data storage
 */
case class Client(
    session: ActorRef,
    account: Account,
    var chars: Map[Entity, (Name, Appearance)],
    var world: World,
    var entity: Option[RichEntity] = None,
    var state: PlayState = IdleInLobby)


trait PlayState
case object IdleInLobby     extends PlayState
case object LoadingMap      extends PlayState
case object Playing         extends PlayState
case object Disconnecting   extends PlayState
