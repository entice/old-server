/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.server._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

import java.net.InetSocketAddress


trait LoginApiSlice extends CoreSlice with ApiSlice {

    // handler actors
    val loginHandler = actorSystem.actorOf(Props(classOf[LoginHandler], reactor), "login")
    val dispatchHandler = actorSystem.actorOf(Props(classOf[DispatchHandler], reactor), "dispatch")
}


case class LoginServer(system: ActorSystem, port: Int) extends Actor 
    with CoreSlice 
    with LoginApiSlice 
    with NetSlice 
    with ActorSlice {

    override lazy val actorSystem = system
    override lazy val localAddress = new InetSocketAddress(port)
}