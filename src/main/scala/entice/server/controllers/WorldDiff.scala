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
class WorldDiff extends Actor with Subscriber with Configurable with Clients with Worlds {

    val subscriptions = classOf[Tick] :: classOf[Flush] :: Nil
    override def preStart { register }


    private var lastDiffTime = System.currentTimeMillis()
    private val minDiffTime = config.minTick


    private def peekTime = System.currentTimeMillis() - lastDiffTime

    private def timeDelta = {
        val diff = peekTime
        lastDiffTime = System.currentTimeMillis()
        diff
    }


    def receive = {
        case MessageEvent(_, Tick()) | MessageEvent(_, Flush()) => update
    }


    def update() {
        if (peekTime < minDiffTime) {
            // TODO: unstable? publish(Schedule(Flush(), Duration(minDiffTime - peekTime, MILLISECONDS)))
            return
        }

        val timeDiff = timeDelta

        worlds.getAll
            .foreach { w => 
                val (changed, added, removed) = w.diff
                clients.getAll
                    .filter  { _.state == Playing }
                    .filter  { _.entity != None }
                    .filter  { _.world == w }
                    .foreach { _.session ! UpdateCommand(timeDiff.toInt, changed, added, removed) }
            }
    }
}