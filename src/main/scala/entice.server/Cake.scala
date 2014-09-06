/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.implementation.cores._
import entice.server.implementation.loggers._

import scala.io.StdIn.readLine


/**
 * This is the server. Period.
 */
object Main
    extends App
    with DefaultServer {

  logger.info("Starting " + System.getProperty("app.env") + " - server on port:" + System.getProperty("server.port"))

  core.init()

  // wait till shutdown
  while (readLine != "exit") {}
  core.shutdown()
}


/** A default server cake with default components */
trait DefaultServer
    extends DefaultLogger
    with DefaultCore
