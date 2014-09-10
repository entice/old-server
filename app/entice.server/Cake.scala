/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.implementation.attributes._
import entice.server.implementation.collections._
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
    extends GlobalSettings {

  private var server: Option[DefaultServer] = None

  override def onStart(app: Application) {
    server = Some(DefaultServer(app))
    Logger.info(s"Starting an entice ${server.get.environment.toString.toLowerCase}-server at ${server.get.config.frontendUrl}")
    server.get.seed()
  }

  override def onStop(app: Application) {
    Logger.info(s"Stopping the entice server.")
  }

  def authenticate(email: String, password: String): Future[Option[Client]] = {
    server match {
      case Some(srv) => srv.clientRegistry.authenticate(email, password)
      case None      => Future.successful(None)
    }
  }

  def deauthenticate(authToken: String) {
    server match {
      case Some(srv) => srv.clientRegistry.remove(authToken) // TODO trigger in eventbus
      case None      => Future.successful(None)
    }
  }

  def authorize(authToken: String): Option[Client] = {
    server.flatMap { _.clientRegistry.get(authToken) }
  }
}


/** A default server cake with default components */
case class DefaultServer(app: Application)
    // basic stuff...
    extends Core
    with Config
    with AccountCollection
    with CharacterCollection
    with ClientRegistry
    with Tracker
    // Worlds... TODO maybe package them all in a trait
    with LobbyWorld
    with HeroesAscent
    with RandomArenas
    with TeamArenas {

  def seed() {
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
}
