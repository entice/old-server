/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.implementation.cores._

import scala.io.StdIn.readLine


/**
 * This is the server. Period.
 */
object Main extends App with DefaultServer {
    // wait till shutdown
    while (readLine != "exit") {}
    core.shutdown()
}


/** A default server cake with default components */
trait DefaultServer
    extends DefaultCore
