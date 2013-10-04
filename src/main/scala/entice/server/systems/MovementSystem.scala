/**
 * For copyright information see the LICENSE document.
 */

package entice.server.systems

import entice.server.world._
import entice.protocol._
import akka.actor._
import shapeless._


class MovementSystem extends System[Position :: Movement :: HNil] with Actor {
    def receive = {
        case _ =>
    }
}