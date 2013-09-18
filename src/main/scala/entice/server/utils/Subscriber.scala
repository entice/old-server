/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import entice.protocol._


/**
 * Actor mixin. Use this to add messagebus registrations conveniently.
 * TODO: Can we make this even more convenient?
 */
trait Subscriber {
    self: Actor =>

    def subscriptions: List[Class[_ <: Message]]
    def messageBus: MessageBus

    def register { 
        subscriptions foreach { messageBus.subscribe(self, _) }
    }
}