/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.login._


object Main extends App {

    val loginServer = LoginServer(8112)

    // wait till shutdown
    while (readLine != "exit") {}
    loginServer.stop
    loginServer.actorSystem.shutdown
}