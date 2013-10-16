/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world

import entice.protocol._
import scala.concurrent.duration._


sealed trait Event extends Typeable

// concerning scheduling: (tick is server internal, flush is done by single components)
case class Tick             ()                                          extends Event                                       
case class Flush            ()                                          extends Event                                       
case class Schedule         (event: Event, after: FiniteDuration)       extends Event

// by the world:
case class Spawned          (entity: RichEntity)                        extends Event
case class Despawned        (entity: RichEntity)                        extends Event

// grouping only:
case class GroupInvite      (sender: RichEntity, recipient: RichEntity) extends Event
case class GroupDecline     (sender: RichEntity, recipient: RichEntity) extends Event
case class GroupAccept      (sender: RichEntity, recipient: RichEntity) extends Event
case class GroupLeave       (sender: RichEntity)                        extends Event
case class GroupKick        (sender: RichEntity, recipient: RichEntity) extends Event

// entity actions:
case class Move             (entity: RichEntity)                        extends Event
case class Chat             (entity: RichEntity, text: String)          extends Event
case class Announcement     (text: String)                              extends Event
case class Animate          (entity: RichEntity, anim: String)          extends Event