/**
 * For copyright information see the LICENSE document.
 */

package entice.server.systems

import entice.server._
import entice.server.world._
import entice.server.utils._
import entice.protocol._
import akka.actor._
import shapeless._


class ChatSystem extends System[HNil] with Actor with Subscriber with Clients {

    val subscriptions = classOf[Chat] :: Nil
    override def preStart { register }


    def receive = {
        case MessageEvent(_, Chat(entity, msg)) =>
            clients.getAll
                .filter  {_.state == Playing}
                .foreach {_.session ! ChatMessage(entity, msg)}
    }
}