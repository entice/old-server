/**
 * For copyright information see the LICENSE document.
 */

package entice.server.game

import entice.server._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import com.typesafe.config.ConfigFactory

import java.net.InetSocketAddress


trait GameApiSlice extends CoreSlice with ApiSlice {
}


case class GameServer(system: ActorSystem, port: Int) extends Actor 
    with CoreSlice 
    with GameApiSlice 
    with NetSlice 
    with ActorSlice {

    override lazy val actorSystem = system
    override lazy val localAddress = new InetSocketAddress(port)
}