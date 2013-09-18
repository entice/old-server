/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.login._

import akka.actor.{ ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scala.concurrent.Await
import scala.concurrent.duration._


object Main extends App {
    import ServerActorSlice._   // holds server Start/Stop messages

    implicit val timeout = Timeout(5 seconds)
    lazy val config = ConfigFactory.parseString("""
        akka {
          loglevel = DEBUG
        }
    """)

    val system = ActorSystem("server-sys", config = config)

    val loginServer = system.actorOf(Props(new LoginServer(system) with AutoStart), "login-server")

    // wait till shutdown
    while (readLine != "exit") {}

    val success = Await.result(loginServer ? Stop, timeout.duration).asInstanceOf[Boolean]
    val msg = if (success) {"Graceful server shutdown done."} else {"Could not shutdown gracefully."}
    system.log.info(msg)
    system.shutdown
}