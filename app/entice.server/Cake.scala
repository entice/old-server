/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.implementation.collections._

import play.api._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext


/**
 * This is the server. Period.
 */
object Global
    extends GlobalSettings {

  var server: Option[DefaultServer] = None

  override def onStart(app: Application) {
    server = Some(DefaultServer(app))
    Logger.info(s"Starting an entice ${server.get.environment.toString.toLowerCase}-server at ${server.get.config.frontendUrl}")
  }

  override def onStop(app: Application) {
    Logger.info(s"Stopping the entice server.")
  }
}


/** A default server cake with default components */
case class DefaultServer(app: Application)
    extends Core
    with Config
    with AccountCollection
    with CharacterCollection
