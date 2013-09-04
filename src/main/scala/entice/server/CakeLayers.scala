/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol.utils._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props, PoisonPill }
import akka.pattern.gracefulStop
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

import java.net.InetSocketAddress


trait CoreSlice {

    lazy val actorSystem = ActorSystem("system")
    lazy val messageBus = new MessageBus()
}


trait ApiSlice extends CoreSlice {

    // standard actors
    lazy val reactor = actorSystem.actorOf(Props(classOf[ReactorActor], messageBus))

    // inherit and add handler actors ;)
}


trait NetSlice extends CoreSlice with ApiSlice {

    lazy val localAddress = new InetSocketAddress(0)
    
    // network stuff
    lazy val acceptor = actorSystem.actorOf(Props(classOf[SessionAcceptorActor], localAddress, reactor))

    def start { acceptor }

    def stop {
        try {
            val stopped: Future[Boolean] = gracefulStop(acceptor, Duration(5, SECONDS))
            Await.result(stopped, Duration(6, SECONDS))
        } catch {
            case e: akka.pattern.AskTimeoutException => 
        }
    }
}