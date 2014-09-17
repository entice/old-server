/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.events

import entice.server._
import entice.server.implementation.attributes._
import entice.server.implementation.entities._
import entice.server.implementation.utils.Coord2D


/**
 * All these events are concerning world updates, general or from/to client.
 */
sealed trait WorldEvent


// General world events --------------------------------------------------------

/**
 * An update is a message that is used internally and externally to track
 * changes of entities and their attributes. It is a special world event that always
 * concerns a certain entity (see `def entity` contract)
 */
sealed trait Update extends TrackingOptions { self: WorldEvent => def entity: Entity }

// World, or entity-view scope:
case class EntityAdd(entity: Entity) extends WorldEvent with Update
case class EntityRemove(entity: Entity) extends WorldEvent with Update

// Entity scope:
case class AttributeAdd(entity: Entity, attribute: Attribute) extends WorldEvent with Update { // note: no typelevel shit possible here
  override def notPropagated = attribute.notPropagated
  override def notVisible = attribute.notVisible
}
case class AttributeRemove(entity: Entity, attribute: Attribute) extends WorldEvent with Update {
  override def notPropagated = attribute.notPropagated
  override def notVisible = attribute.notVisible
}

// Attribute scope:
case class AttributeChange(entity: Entity, older: Attribute, newer: Attribute) extends WorldEvent with Update {
  override def notPropagated = older.notPropagated
  override def notVisible = older.notVisible
}


// C->S S->C world load --------------------------------------------------------

/**
 * Initiates the map load process, or fails.
 * If this packet is send, it is garuanteed to be followed by:
 * 1. Updates to the attributes of the player.
 * 2. All entity-IDs of all entities on the map
 * 3. All attributes of the entities that are visible
 */
case class PlaySuccess(chara: Entity) extends WorldEvent // S->C


// C->S S->C world messages ----------------------------------------------------


/** Bi-directional message for ingame chats of all kinds. Client-to-client communication */
case class ChatMessage(sender: Entity, message: String, channel: ChatChannel.Value) extends WorldEvent
/** Issues a server command. Use /helpme ingame to show available commands */
case class IngameCommand(command: String, args: List[String]) extends WorldEvent // C->S
/** General server messages (announcements etc.) */
case class ServerMessage(message: String) extends WorldEvent // S->C


// C->S world requests ---------------------------------------------------------


/** Request to change direction and start moving in the given direction. Stop if dir is vec(0,0) */
case class MoveRequest(direction: Coord2D) extends WorldEvent
/** Request to fuse my group with another one (even if you do not yet have a group). Only as leader */
case class GroupMergeRequest(target: Entity) extends WorldEvent
/** Request to kick a player from the group. Only as leader, only if play in your group */
case class GroupKickRequest(target: Entity) extends WorldEvent


// S->C world updates (additionally to above stuff) ----------------------------


case class EventInterval(timeDelta: Int) extends WorldEvent


