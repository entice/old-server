/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.attributes._
import entice.server.enums.ChatChannel
import entice.server.handles.{Clients, Entities}
import entice.server.utils.Coord2D
import julienrf.variants.Variants
import play.api.libs.json.Format


trait WorldEvents { self: Clients with Entities =>
  import clients.ClientHandle
  import entities.EntityHandle

  /**
   * All these events are concerning world updates, general or from/to client.
   */
  sealed trait WorldEvent


  // Server internal - not propagated --------------------------------------------

  sealed trait SessionEvent
  case class PlayerJoin(client: ClientHandle, chara: String) extends SessionEvent
  case class PlayerQuit(client: ClientHandle) extends SessionEvent


  // General world events --------------------------------------------------------

  /**
   * An update is a message that is used internally and externally to track
   * changes of entities and their attributes. It is a special world event that always
   * concerns a certain entity (see `def entity` contract)
   */
  sealed trait Update extends TrackingOptions { self: WorldEvent => def entity: EntityHandle }

  // World, or entity-view scope:
  case class EntityAdd(entity: EntityHandle) extends WorldEvent with Update
  case class EntityRemove(entity: EntityHandle) extends WorldEvent with Update

  // Entity scope:
  case class AttributeAdd(entity: EntityHandle, attribute: Attribute) extends WorldEvent with Update { // note: no typelevel shit possible here
    override def notPropagated = attribute.notPropagated
    override def notVisible = attribute.notVisible
  }
  case class AttributeRemove(entity: EntityHandle, attribute: Attribute) extends WorldEvent with Update {
    override def notPropagated = attribute.notPropagated
    override def notVisible = attribute.notVisible
  }

  // Attribute scope:
  case class AttributeChange(entity: EntityHandle, older: Attribute, newer: Attribute) extends WorldEvent with Update {
    override def notPropagated = older.notPropagated
    override def notVisible = older.notVisible
  }


  // C->S S->C world load --------------------------------------------------------

  /**
   * Initiates the map load process, or fails.
   * If this packet is send, it is guaranteed to be followed by:
   * 1. Updates to the attributes of the player.
   * 2. All entity-IDs of all entities on the map
   * 3. All attributes of the entities that are visible
   */
  case class PlaySuccess(chara: EntityHandle) extends WorldEvent // S->C


  // C->S S->C world messages ----------------------------------------------------

  /** Bi-directional message for ingame chats of all kinds. Client-to-client communication */
  case class ChatMessage(sender: EntityHandle, message: String, channel: ChatChannel.Value) extends WorldEvent
  /** Issues a server command. Use /helpme ingame to show available commands */
  case class IngameCommand(command: String, args: List[String]) extends WorldEvent // C->S
  /** General server messages (announcements etc.) */
  case class ServerMessage(message: String) extends WorldEvent // S->C


  // C->S world requests ---------------------------------------------------------

  /** Request to change direction and start moving in the given direction. Stop if dir is vec(0,0) */
  case class MoveRequest(direction: Coord2D) extends WorldEvent
  /** Request to fuse my group with another one (even if you do not yet have a group). Only as leader */
  case class GroupMergeRequest(target: EntityHandle) extends WorldEvent
  /** Request to kick a player from the group. Only as leader, only if play in your group */
  case class GroupKickRequest(target: EntityHandle) extends WorldEvent


  // C->S S->C general world updates (additionally to above stuff) ----------------------------

  case class EventInterval(timeDelta: Int) extends WorldEvent // S->C in ms
  case class Ping(roundTripTime: Int = 100) extends WorldEvent // Bi-directional, in ms
  case object Pong extends WorldEvent // Bi-directional


  // Serialization...

  implicit val worldEventFormat: Format[WorldEvent] = Variants.format[WorldEvent]("type")
}
