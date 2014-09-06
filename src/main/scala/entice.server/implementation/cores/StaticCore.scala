/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.cores

import entice.server._
import entice.server.implementation.events.EventBus

import akka.actor.ActorSystem


/**
 * Simple implementation, uses default hard coded vals only.
 */
trait StaticCore extends Core { self: Logger =>

  lazy val actorSystem = ActorSystem("entice-server", defaultAkkaConfig)
  lazy val eventBus = new EventBus()
}
