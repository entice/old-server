/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.behaviours.Tracking
import entice.server.controllers._
import entice.server.handles.{Clients, Entities}
import entice.server.worlds.{WorldBase, WorldWatchers, WorldTracking, GuildWarsWorlds}
import models._
import play.api._


/**
 * This is the server. Period.
 */
object Global
    extends GlobalSettings
    with DefaultServer {

  lazy val app = Play.current

  override def onStart(app: Application) {
    Logger.info(s"Starting an entice ${environment.toString.toLowerCase}-server at ${config.frontendUrl}")
    seeder.seed()
  }

  override def onStop(app: Application) {
    Logger.info(s"Stopping the entice server.")
  }
}


/** A default server cake with default components */
trait DefaultServer
    // Core and DB
    extends Core
    with Accounts
    with Characters
    // Server level services ...
    with Config
    with Seed
    with Tracker
    with Security
    // World stuff...
    with Clients
    with Entities with Attributes with Behaviours
    with Tracking // behaviours
    with GuildWarsWorlds
    with WorldEvents
    // Controllers
    with AuthController
    with CharacterController
    with ClientController
    with LobbyController
    with WorldController
