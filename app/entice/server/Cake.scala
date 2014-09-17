/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import models._

import entice.server.implementation.attributes._
import entice.server.implementation.worlds._

import play.api._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent._
import duration._


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
    // core and DB
    extends Core
    with Accounts
    with Characters
    // server level services
    with Config
    with Seed
    with ClientRegistry
    with Tracker
    // Worlds... TODO maybe package them all in a trait
    with HeroesAscent
    with RandomArenas
    with TeamArenas
