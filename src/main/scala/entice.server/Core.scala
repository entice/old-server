/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.implementation.events.EventBus

import akka.actor.ActorSystem

import com.typesafe.config.{ Config, ConfigFactory }


/**
 * Server core. Provides the most basic services and requires nothing
 */
trait Core { self: Logger =>
  def actorSystem: ActorSystem
  def eventBus: EventBus

  object core {
    def init() {
      self.logger.info("Starting the server core.")
      // forcing lazy init if any
      actorSystem
      eventBus
      self.logger.debug("Akka log-level: " + actorSystem.settings.config.getString("akka.loglevel"))
    }

    def shutdown() {
      self.logger.info("Attempting graceful shutdown of the server core.")
      actorSystem.shutdown
    }
  }

  protected lazy val defaultAkkaConfig: Config = {
    self.logger.warn("Falling back to default akka config!")
    ConfigFactory.parseString("""
      akka {
        loggers = ["akka.event.slf4j.Slf4jLogger"]
        loglevel = DEBUG
        log-dead-letters-during-shutdown = off
        logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
      }
    """)
  }
}
