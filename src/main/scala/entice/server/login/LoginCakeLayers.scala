/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.server._

import akka.actor.{ ActorRef, ActorSystem, Props }
import com.typesafe.config.ConfigFactory
import com.softwaremill.macwire.Macwire

import java.net.InetSocketAddress


trait LoginApiSlice extends CoreSlice with ApiSlice with Macwire {

    // handler actors
    lazy val loginFactory = wire[LoginFactory]
    loginFactory.createAndSubscribe()

    lazy val dispatchFactory = wire[DispatchFactory]
    dispatchFactory.createAndSubscribe()
}


case class LoginServer(port: Int) extends CoreSlice with LoginApiSlice with NetSlice {

    lazy val config = ConfigFactory.parseString("""
        akka {
          loglevel = DEBUG
        }
    """)
      
    override lazy val actorSystem = ActorSystem("default", config = config)
    override lazy val localAddress = new InetSocketAddress(port)

    start
}