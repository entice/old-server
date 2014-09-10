/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.entities

import entice.server._
import entice.server.implementation.attributes._
import entice.server.implementation.behaviours._
import entice.server.implementation.utils._
import entice.server.implementation.worlds._

import scala.concurrent.ExecutionContext


/**
 * Special lobby entity. This does nothing but can still hold attributes.
 * Can only be instantiated by the Lobby world.
 */
case class LobbyEntity(
    world: LobbyWorld#WorldLike,
    initialAttr: Option[ReactiveTypeMap[Attribute]] = None) extends Entity {

  import ExecutionContext.Implicits.global

  val attr = initialAttr.getOrElse(new ReactiveTypeMap[Attribute]())
  val behav = new ReactiveTypeMap[Behaviour]()
}
