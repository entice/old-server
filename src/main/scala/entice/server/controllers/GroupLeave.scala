/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server._, Net._
import entice.server.world._
import entice.server.utils._
import entice.server.database._
import entice.protocol._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import scala.language.postfixOps


class GroupLeave extends Actor with Subscriber with Clients {

    val subscriptions = classOf[GroupKickRequest] :: classOf[GroupKickRequest] :: Nil
    override def preStart { register }


    def receive = {

        case MessageEvent(session, GroupKickRequest(target)) =>
            clients.get(session)  match {

                // if the client is a member, the target must be us
                case Some(client @ Client(_, _, _, world, entity, state))
                    if (state == Playing 
                    && entity != None
                    && entity.get.get[GroupLeader] == None
                    && entity.get.get[GroupMember] != None
                    && entity.get.entity == target
                    && world.contains(entity.get[GroupMember].leader) 
                    && world.getComps(entity.get[GroupMember].leader).get.contains[GroupLeader]) =>

                    leaveGroupAsMember(entity.get)
                    

                // if the client is leader, and the target is still us, and the group is not empty
                case Some(client @ Client(_, _, _, world, entity, state))
                    if (state == Playing 
                    && entity != None
                    && entity.get.get[GroupMember] == None
                    && entity.get.get[GroupLeader] != None
                    && entity.get.entity == target
                    && entity.get[GroupLeader].members != Nil) =>

                    leaveGroupAsLeader(entity.get)

                // if the client is leader, and the target is NOT us, and the target is part of our group
                case Some(client @ Client(_, _, _, world, entity, state))
                    if (state == Playing 
                    && entity != None
                    && entity.get.get[GroupMember] == None
                    && entity.get.get[GroupLeader] != None
                    && entity.get.entity != target
                    && world.contains(target) 
                    && entity.get[GroupLeader].members != Nil 
                    && entity.get[GroupLeader].members.contains(target) 
                    && world.getComps(target).get.contains[GroupMember]) =>

                    kickGroupMember(entity.get, world.getRich(target).get)

                case _ =>
                    session ! Failure("Not logged in, or not playing, or no groupmember or invalid target.")
                    // TODO: Is this necessary: session ! Kick
            }
    }


    def leaveGroupAsMember(me: RichEntity) {
        // we will need to clean up the old group later on...
        val recipient = me.world.getRich(me[GroupMember].leader).get
        val recLeader = recipient[GroupLeader]

        // get us a new group
        me.drop[GroupMember]
        me.set(GroupLeader())

        // remove us from the old group
        recipient.set(recLeader.copy(members = recLeader.members filterNot { _ == me.entity }))
    }


    def leaveGroupAsLeader(me: RichEntity) {
        // we will need to clean up the old group later on...
        val oldLeader = me[GroupLeader]
        val recipient = me.world.getRich(oldLeader.members(0)).get
        
        // make recipient the new leader...
        recipient.drop[GroupMember]
        recipient.set(oldLeader.copy(members = oldLeader.members filterNot { _ == recipient.entity }))

        // update other team members to have a new leader
        recipient[GroupLeader].members
            .filter  { mem => me.world.contains(mem) }
            .map     { mem => me.world.getRich(mem).get }
            .foreach { mem => mem.set(GroupMember(recipient)) }

        // get us a new group
        me.drop[GroupMember]
        me.set(GroupLeader())        
    }


    def kickGroupMember(me: RichEntity, other: RichEntity) {
        // get the former member its own group
        other.drop[GroupMember]
        other.set(GroupLeader())

        // remove it from our group
        val oldLeader = me[GroupLeader]
        me.set(oldLeader.copy(members = oldLeader.members filterNot { _ == other.entity }))
    }
}