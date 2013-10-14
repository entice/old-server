/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world

import entice.protocol._
import scala.concurrent.duration._


sealed trait Event extends Typeable


case class Tick() extends Event                                        // from the system
case class Flush() extends Event                                       // from single components
case class Schedule(event: Event, after: FiniteDuration) extends Event

case class ChangeSet(entity: RichEntity, changed: Map[String, Component], removed: List[String]) extends Event

case class Spawned(entity: RichEntity) extends Event
case class Despawned(entity: RichEntity) extends Event

case class Move(entity: RichEntity) extends Event
case class Chat(entity: RichEntity, text: String) extends Event
case class Animate(entity: RichEntity, anim: String) extends Event
case class GroupInvite(sender: RichEntity, recipient: RichEntity) extends Event // reply with entice.protocol stuff