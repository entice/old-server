/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.cores

import entice.server._
import entice.server.implementation.events.EventBus

import akka.actor.ActorSystem
import com.typesafe.config.{ Config, ConfigFactory }

import java.util.Scanner
import java.io.File
import scala.io.Source
import scala.util.Try


/**
 * Server core. Provides the most basic services. Does nothing, requires nothing
 */
trait DefaultCore extends Core { self: Logger =>

  lazy val akkaConfigFile = "conf/akka.conf"
  lazy val actorSystem = ActorSystem("entice-server", parseAkkaConfig(akkaConfigFile) getOrElse defaultAkkaConfig)
  lazy val eventBus = new EventBus()

  private def parseAkkaConfig(file: String): Option[Config] = {
    (for {
      src <- Try(Source.fromFile(file).mkString.trim)
      res <- Try(ConfigFactory.parseString(src))
    } yield { res }).toOption
  }
}
