/**
 * For copyright information see the LICENSE document.
 */

// package entice

// import play.api.libs.json._
// import julienrf.variants.Variants


// // Hint: Network messages are further down.

// package object protocol {

//   // Import all enum formats
//   import MoveState._
//   import ChatChannel._
//   import CharacterAnimation._
//   import Map._


//   /** Simple type aliases. Does not affect serialization */
//   type EntityId = Int
//   type AttributeName = String


//   /** Resembles a 2d vector or point (depending on the context) */
//   case class Coord2D(x: Float, y: Float)
//   implicit val coord2DFormat = Json.format[Coord2D]


//   /** Data used for character-lobby viewing only */
//   case class CharacterView(entityId: EntityId, name: Name, appearance: Appearance)
//   implicit val nameFormat = Json.format[Name]
//   implicit val appearanceFormat = Json.format[Appearance]
//   implicit val characterViewFormat = Json.format[CharacterView]


//   /**
//    * A general network message.
//    * This trait is split up into special subtraits to further refine
//    * what kind of message is transferred trough the network.
//    *
//    * TODO: Due to current limitations in the serialization framework we try to
//    * avoid using Arrays of Traits whereever possible
//    *
//    * Look into the messages for further documentation, and look for the implented traits
//    * which stand as kind of self-docuemtnation for the messages.
//    */
//   sealed trait Message
//   sealed trait IncomingMessage extends Message // marker for Client-to-Server messages
//   sealed trait OutgoingMessage extends Message // marker for Server-to-Client messages
//   sealed trait CanFail extends OutgoingMessage // marker for messages that can fail with Failure(...)

//   /** A general error occured. Avoids error codes - sends the message right away */
//   case class Failure(error: String = "An unkown error occured.")  extends Message with OutgoingMessage


//   /**
//    * Login process only
//    */
//   sealed trait LoginMessage extends Message
//   /** Request with credentials */
//   case class LoginRequest(email: String, password: String) extends LoginMessage with IncomingMessage
//   /** Answer with this or fail the login */
//   case class LoginSuccess(chars: List[CharacterView]) extends LoginMessage with OutgoingMessage with CanFail


//   /**
//    * Messages allowed in lobby only
//    */
//   sealed trait LobbyMessage extends Message
//   /** Request to create a new character (no pvp/pve yet) on the server */
//   case class CharCreateRequest(name: Name, appearance: Appearance) extends LobbyMessage with IncomingMessage
//   /** Request to delete a specified character, will not be answered */
//   case class CharDelete(chara: EntityId) extends LobbyMessage with IncomingMessage
//   /** Answer to creation process, or failure */
//   case class CharCreateSuccess(chara: EntityId) extends LobbyMessage with OutgoingMessage with CanFail


//   /**
//    * World enter, change and leave stuff
//    */
//   sealed trait PlayMessage extends Message
//   /** Needs to be send from lobby. Request to enter the world the char is in. */
//   case class PlayRequest(chara: EntityId) extends PlayMessage with IncomingMessage
//   /** Needs to be send from world. Request to change to another world (map instance) */
//   case class PlayChangeMap(map: Map) extends PlayMessage  with IncomingMessage
//   /** Needs to be send from world. Request to change back to Lobby */
//   case class PlayQuit() extends PlayMessage with IncomingMessage
//   /**
//    * Initiates the map load process, or fails.
//    * If this packet is send, it is garuanteed to be followed by:
//    * 1. Updates to the attributes of the player.
//    * 2. All entity-IDs of all entities on the map
//    * 3. All attributes of the entities that are visible
//    */
//   case class PlaySuccess(map: Map, chara: EntityId) extends PlayMessage with OutgoingMessage with CanFail


//   /**
//    * General ingame messages, for chat, maintenance etc.
//    */
//   sealed trait IngameMessage
//   /** Bi-directional message for ingame chats of all kinds. Client-to-client communication */
//   case class ChatMessage(sender: EntityId, message: String, channel: ChatChannel) extends IngameMessage with IncomingMessage with OutgoingMessage
//   /** Issues a server command. Use /helpme ingame to show available commands */
//   case class IngameCommand(command: String, args: List[String]) extends IngameMessage with IncomingMessage
//   /** General server messages (announcements etc.) */
//   case class ServerMessage(message: String) extends IngameMessage with OutgoingMessage


//   /**
//    * Client-side update requests on the game state only
//    */
//   sealed trait UpdateRequestMessage extends Message with IncomingMessage
//   /** Request to change direction and start moving in the given direction. Stop if dir is vec(0,0) */
//   case class MoveRequest(direction: Coord2D) extends UpdateRequestMessage
//   /** Request to fuse my group with another one (even if you do not yet have a group). Only as leader */
//   case class GroupMergeRequest(target: EntityId) extends UpdateRequestMessage
//   /** Request to kick a player from the group. Only as leader, only if play in your group */
//   case class GroupKickRequest(target: EntityId) extends UpdateRequestMessage


//   /**
//    * Server-side update commands (generally: a push of updates on the game state)
//    * Updates are in order and can be distinguished timewise by the event interval packet.
//    */
//   sealed trait ServerCommandMessage extends Message with OutgoingMessage
//   /** Splits the event stream into distinct and discrete event interval time windows */
//   case class EventInterval(timeDelta: Int) extends ServerCommandMessage
//   /** World scope: Adds an entity to this world. Sent also if entity not in visible range. */
//   case class AddEntity(entity: EntityId) extends ServerCommandMessage
//   /** World scope: Removes an entity to this world. Sent also if entity not in visible range. */
//   case class RemoveEntity(entity: EntityId) extends ServerCommandMessage
//   /** Entity scope: Add a new attribute to an entity */
//   case class AddAttribute(entity: EntityId, attribute: NetworkAttribute) extends ServerCommandMessage
//   /** Entity scope: Remove an attribute from an entity */
//   case class RemoveAttribute(entity: EntityId, attribute: AttributeName) extends ServerCommandMessage
//   /** Entity scope: Change an attribute of an entity to a new state. TODO Really needed? */
//   case class ChangeAttribute(entity: EntityId, older: NetworkAttribute, newer: NetworkAttribute) extends ServerCommandMessage
// }


