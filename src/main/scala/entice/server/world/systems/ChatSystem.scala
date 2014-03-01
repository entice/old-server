/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world.systems

import entice.server._
import entice.server.world._
import entice.server.utils._
import entice.protocol._
import akka.actor._
import shapeless._


class ChatSystem extends System[HNil] with Actor with Subscriber with Clients {

    val subscriptions = classOf[Chat] :: classOf[Announcement] :: Nil
    override def preStart { register }


    def receive = {
        // ALL chat message
        case MessageEvent(_, Chat(entity, msg, ChatChannels.All)) =>
            clients.getAll
                .filter  { _.state == Playing }
                .filter  { _.world == entity.world }
                .foreach { _.session ! ChatMessage(entity, msg, ChatChannels.All.toString) }

        // GROUP chat message (either from the perspective of a member or leader)
        case MessageEvent(_, Chat(entity, msg, ChatChannels.Group)) =>
            val leaderEntity : Entity = entity.get[GroupMember] match {
                case Some(group) => group.leader
                case _           => entity // unrich automatically
            }
            val leader : Option[GroupLeader] = entity.world.getRich(leaderEntity) match {
                case Some(rich)  => rich.get[GroupLeader]
                case _           => None
            }

            leader map { l : GroupLeader =>
                // send it to the members
                l.members.foreach { clients.get(_) map { 
                    _.session ! ChatMessage(entity, msg, ChatChannels.Group.toString) }
                }
                // and send it to the sender (or not if it has no client)
                l.members match {
                    case Nil => clients.get(leaderEntity) map { 
                        _.session ! ServerMessage("Nobody can hear you. QQ") }
                    case _   => clients.get(leaderEntity) map { 
                        _.session ! ChatMessage(entity, msg, ChatChannels.Group.toString) }
                }
            }

        case MessageEvent(_, Announcement(msg)) =>
            clients.getAll
                .filter  { _.state == Playing }
                .foreach { _.session ! ServerMessage(msg) }
    }
}