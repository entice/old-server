/**
 * For copyright information see the LICENSE document.
 */

package entice.server.game.entitysystems

import entice.server.game._

import akka.actor.Actor


class MovementSystem extends Actor {

    def receive = {
        case Tick() =>
    }
}