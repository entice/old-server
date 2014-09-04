/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.implementation.events.EventBus

import akka.actor.ActorSystem
import akka.event.Logging

import com.typesafe.config.{ Config, ConfigFactory }


/**
 * Server core. Provides the most basic services and requires nothing
 */
trait Core {
  def actorSystem: ActorSystem
  def eventBus: EventBus

  protected lazy val defaultAkkaConfig: Config = {
    Logging.getLogger(actorSystem, this).warning("Falling back to default akka config!")
    ConfigFactory.parseString("""
      akka {
        loglevel = DEBUG
        log-dead-letters-during-shutdown = off
      }
    """)
  }
}
