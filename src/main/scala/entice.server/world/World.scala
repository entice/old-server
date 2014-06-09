/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import events.EventBus

import akka.actor.ActorSystem


class World(
    val actorSystem: ActorSystem, 
    val eventBus: EventBus = new EventBus,
    val tracker: Tracker = new Tracker {}) {
}