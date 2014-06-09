/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import events.EventBus
import behaviours._

import akka.actor.ActorSystem


trait BehavioursModule {
  val behaviours =
    TrackingFactory ::
    Nil
}


class World(
    val actorSystem: ActorSystem, 
    val eventBus: EventBus = new EventBus,
    val tracker: Tracker = new Tracker {}) {
}