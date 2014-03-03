/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server._, Net._
import entice.server.utils._
import entice.server.world._
import entice.protocol._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import scala.concurrent.duration._
import scala.language.postfixOps


/**
 * TODO: This needs to be changed when multiple worlds are implemented,
 * so that a flush concerns only one specific world.
 */
class WorldDiff(
    stopWatch : StopWatch) extends Actor with Subscriber with Clients with Worlds {

    val subscriptions = classOf[Tick] :: classOf[PushUpdate] :: Nil
    override def preStart { register }

    private val minDiffTime = Config.get.minUpdate


    def receive = {
        case MessageEvent(_, PushUpdate()) => update
    }


    def update() {
        val timeDiff = stopWatch.current
        if (timeDiff < minDiffTime) {
            // TODO: unstable? publish(Schedule(Flush(), Duration(minDiffTime - peekTime, MILLISECONDS)))
            return
        }

        worlds.getAll
            .foreach { w => 
                val (changed, added, removed) = w.diff
                clients.getAll
                    .filter  { _.state == Playing }
                    .filter  { _.entity != None }
                    .filter  { _.world == w }
                    .foreach { _.session ! UpdateCommand(timeDiff.toInt, changed, added, removed) }
            }

        stopWatch.reset
    }
}