/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world.systems

import entice.server._
import entice.server.world._
import entice.server.utils._
import entice.protocol._
import akka.actor._
import shapeless._
import scala.concurrent.duration._


class SchedulingSystem extends System[HNil] with Actor with Subscriber {

    import context._

    val subscriptions = classOf[Schedule] :: Nil
    override def preStart { register }


    def receive = {
        case MessageEvent(_, Schedule(event, after)) =>
            context.system.scheduler
                .scheduleOnce(after)(publish(event))
    }
}