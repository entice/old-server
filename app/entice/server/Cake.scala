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
object Cake
    extends GlobalSettings
    with DefaultServer {

  lazy val app = Play.current

  override def onStart(app: Application) {
    Logger.info(s"Starting an entice ${environment.toString.toLowerCase}-server at ${config.frontendUrl}")
    seeder.seed()

    lifecycle.serverStart()
  }

  override def onStop(app: Application) {
    lifecycle.serverStop()
    Logger.info(s"Stopped the entice server.")
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
    with Entities with Tracking
    with GuildWarsWorlds
    // Controllers
    with AuthController
    with CharacterController
    with ClientController
    with LobbyController
    with WorldController
