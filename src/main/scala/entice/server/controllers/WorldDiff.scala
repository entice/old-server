/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server._, Net._
import entice.server.utils._
import entice.server.world._
import entice.protocol._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import scala.language.postfixOps


/**
 * TODO: This needs to be changed when multiple worlds are implemented,
 * so that a flush concerns only one specific world.
 */
class WorldDiff extends Actor with Subscriber with Clients with Worlds {

    val subscriptions = classOf[Tick] :: classOf[Flush] :: Nil
    override def preStart { register }


    private var lastDiffTime = System.nanoTime()
    private val minDiffTime = 10 // milliseconds


    private def peekTime = {
        val current = System.nanoTime()
        ((current - lastDiffTime) / 1000000) toInt
    }


    private def timeDelta = {
        val diff = peekTime
        lastDiffTime = System.nanoTime()
        diff
    }


    def receive = {
        case MessageEvent(_, Tick()) | MessageEvent(_, Flush()) => update
    }


    def update() {
        if (peekTime < minDiffTime) return

        worlds.getAll
            .foreach { w => 
                val (changed, added, removed) = w.diff 
                clients.getAll // should be getAllOfThisWorld
                    .filter  {_.state == Playing}
                    .foreach {_.session ! UpdateCommand(timeDelta, changed, added, removed)}
            }
    }
}