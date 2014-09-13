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
    if (environment == Development) { seedDevelopment() }
    if (environment == Production)  { seedProduction() }
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
    with ClientRegistry
    with Tracker
    // Worlds... TODO maybe package them all in a trait
    with HeroesAscent
    with RandomArenas
    with TeamArenas {

  def seedDevelopment() {
    Logger.info("Seeding some server data...")
    val timeout: FiniteDuration = DurationInt(10).seconds
    val acc1 = Account("root@entice.ps", "root")
    val acc2 = Account("test@entice.ps", "test")

    Await.ready(accounts.dropCollection(), timeout)
    Await.ready(characters.dropCollection(), timeout)

    accounts.create(acc1)
    accounts.create(acc2)
    characters.create(Character(acc1.id, "Test Char", Appearance()))
    characters.create(Character(acc1.id, "Abc Def", Appearance()))
    characters.create(Character(acc2.id, "Hello Again", Appearance()))
    characters.create(Character(acc2.id, "Hans Wurst", Appearance()))

    Logger.info("Seeding done.")
  }


  def seedProduction() {
    Logger.info("Seeding some server data...")
    val timeout: FiniteDuration = DurationInt(10).seconds
    val acc1 = Account("root@entice.ps", "root")

    Await.ready(accounts.dropCollection(), timeout)
    Await.ready(characters.dropCollection(), timeout)

    accounts.create(acc1)
    Logger.info("Seeding done.")
  }
}
