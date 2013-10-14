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


class GroupJoin extends Actor with Subscriber with Clients {

    val subscriptions = classOf[GroupMergeRequest] :: classOf[GroupKickRequest] :: Nil
    override def preStart { register }


    def receive = {

        case MessageEvent(session, GroupMergeRequest(target)) =>
            clients.get(session)  match {
                case Some(client @ Client(_, _, _, world, entity, state))
                    if (state == Playing 
                    && entity != None
                    && entity.get.get[GroupLeader] != None
                    && entity.get.entity != target
                    && world.contains(target) 
                    && (world.getComps(target).get.contains[GroupLeader]
                    ||  world.getComps(target).get.contains[GroupMember])) =>

                    // get the maxsize of this map
                    val groupSize = Maps.withMapName(world.name).groupSize

                    // get the actual recipient of the request
                    var recipient = 
                        if (world.getComps(target).get.contains[GroupLeader]) world.getRich(target).get
                        else world.getRich(world.getComps(target).get[GroupMember].leader).get

                    // check if the size would be OK
                    if (groupSize >= 
                        entity.get[GroupLeader].members.length + 
                        recipient [GroupLeader].members.length) {

                        join(entity.get, recipient)
                    }               
 
                case _ =>
                    session ! Failure("Not logged in, or not playing, or no groupleader or invalid target.")
                    // TODO: Is this necessary: session ! Kick
            }
    }


    def join(me: RichEntity, other: RichEntity) {

        val meLeader = me[GroupLeader]
        var otherLeader = other[GroupLeader]

        // case 1 accepting a request
        if (meLeader.joinRequests.contains(other.entity)) {
            // make me and my members members of other
            otherLeader = otherLeader.copy(members = otherLeader.members ::: List(me.entity))
            otherLeader = otherLeader.copy(members = otherLeader.members ::: meLeader.members)
            // new leader for my members
            meLeader.members
                .filter  { inv => me.world.contains(inv) }
                .map     { inv => me.world.getRich(inv).get }
                .filter  { inv => inv.get[GroupMember] != None }
                .foreach { inv => inv.set(GroupMember(other.entity)) }
            // clean up my invites
            meLeader.invited
                .filter  { inv => me.world.contains(inv) }
                .map     { inv => me.world.getRich(inv).get }
                .filter  { inv => inv.get[GroupLeader] != None }
                .foreach { inv => 
                    inv.set(inv[GroupLeader].copy(
                        joinRequests = inv[GroupLeader].joinRequests filterNot { _ == me.entity })) 
                }
            // new leader for me (swap components)
            me.drop[GroupLeader]
            me.set(GroupMember(other.entity))
            // update invitation of recipient
            other.set(otherLeader.copy(invited = otherLeader.invited filterNot { _ == me.entity }))
        }
        // case 2 sending a invitation
        else {
            // update my invited if needed
            if (!meLeader.invited.contains(other.entity)) {
                me.set(meLeader.copy(invited = other.entity :: meLeader.invited))
            }
            // update recipients join requests if needed
            if (!otherLeader.joinRequests.contains(me.entity)) {
                other.set(otherLeader.copy(joinRequests = me.entity :: otherLeader.joinRequests))
            }

            publish(GroupInvite(me, other))
        }
    }
}