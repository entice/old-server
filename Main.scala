/**
 * For copyright information see the LICENSE document.
 */

package entice

import entice.server._

import akka.actor.{ ActorSystem, Props }
import com.typesafe.config.ConfigFactory


object Main extends App {
    
    lazy val config = ConfigFactory.parseString("""
        akka {
          loglevel = DEBUG
          log-dead-letters-during-shutdown = off
        }
    """)

    val system = ActorSystem("server-sys", config = config)

    val server = system.actorOf(Props[ServerActorSlice], "server")

    // wait till shutdown
    while (readLine != "exit") {}
    system.shutdown
}