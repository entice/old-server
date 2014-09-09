/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.implementation.events.EventBus

import akka.actor.ActorSystem

import play.api._
import play.libs._


/**
 * Server core. Provides the most basic services and requires nothing
 */
trait Core {

  def app: Application
  lazy val actorSystem: ActorSystem = Akka.system // play's actor sys
  lazy val eventBus: EventBus = new EventBus()
  lazy val environment: Environment = {
    app.mode match {
      case Mode.Dev  => Development
      case Mode.Test => Test
      case Mode.Prod => Production
      case _         => Production
    }
  }

  sealed trait Environment { def value: String }
  case object Development extends Environment { val value = "DEV" }
  case object Test        extends Environment { val value = "TEST" }
  case object Production  extends Environment { val value = "PROD" }
}
