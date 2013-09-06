/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.login._

import akka.actor.{ ActorSystem, Props }
import com.typesafe.config.ConfigFactory


object Main extends App {
    import ActorSlice._   // holds server Start/Stop messages

    lazy val config = ConfigFactory.parseString("""
        akka {
          loglevel = DEBUG
        }
    """)

    val system = ActorSystem("server-sys", config = config)

    val loginServer = system.actorOf(Props(new LoginServer(system, 8112) with AutoStart))

    // wait till shutdown
    while (readLine != "exit") {}
    loginServer ! Stop
    system.shutdown
}