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
import scala.concurrent.duration._
import scala.language.postfixOps


/**
 * TODO: With a more advanced System, this should have the following aspect:
 * must either (have(GroupLeader) or have(GroupMember))
 *
 * TODO: Idea: to unclutter the entity creation process, add the GroupLeader
 * component to the entity upon spawning. Possible problem: might cause problems
 * with the client if it expects the GroupLeader to be present. (Specification issue)
 */
class GroupSystem extends System[HNil] with Actor with Subscriber {

    val subscriptions = 
        //classOf[Spawned] ::
        classOf[Despawned] ::
        classOf[GroupInvite] ::
        classOf[GroupDecline] ::
        classOf[GroupAccept] ::
        classOf[GroupLeave] ::
        classOf[GroupKick] ::
        Nil
    override def preStart { register }


    def receive = {

        // invite
        case MessageEvent(_, GroupInvite(me, other)) 
            if (me != other
            &&  me   .get[GroupLeader] != None
            &&  other.get[GroupLeader] != None) =>
            invite(me, other)

        // decline (either invited or join-request, doesnt matter)
        case MessageEvent(_, GroupDecline(me, other))
            if (me != other
            &&  me   .get[GroupLeader] != None
            &&  other.get[GroupLeader] != None) =>
            decline(me, other)

        // accept
        case MessageEvent(_, GroupAccept(me, other))
            if (me != other
            &&  me   .get[GroupLeader] != None
            &&  other.get[GroupLeader] != None) =>
            accept(me, other)

        // leave (1.1: Member)
        case MessageEvent(_, GroupLeave(me))
            if me.get[GroupMember] != None =>
            leaveAsMember(me)

        // leave (1.2: Member + Despawn)
        case MessageEvent(_, Despawned(world, me, comps))
            if comps.contains[GroupMember] =>
            despawnAsMember(world, me, comps[GroupMember])

        // leave (2.1: Leader)
        case MessageEvent(_, GroupLeave(me))
            if me.get[GroupLeader] != None =>
            leaveAsLeader(me)

        // leave (2.2: Leader + Despawn)
        case MessageEvent(_, Despawned(world, me, comps))
            if comps.contains[GroupLeader] =>
            despawnAsLeader(world, me, comps[GroupLeader])

        // kick
        case MessageEvent(_, GroupKick(me, other))
            if (me != other
            &&  me   .get[GroupLeader] != None
            &&  other.get[GroupMember] != None) =>
            kick(me, other)
    }


    def invite(me: RichEntity, other: RichEntity) {
        val meLeader = me[GroupLeader]
        var otherLeader = other[GroupLeader]

        // update my invited if needed
        if (!meLeader.invited.contains(other.entity)) {
            me.set(meLeader.copy(invited = other.entity :: meLeader.invited))
        }
        // update recipients join requests if needed
        if (!otherLeader.joinRequests.contains(me.entity)) {
            other.set(otherLeader.copy(joinRequests = me.entity :: otherLeader.joinRequests))
        }
    }


    def decline(me: RichEntity, other: RichEntity) {
        val meLeader = me[GroupLeader]
        val otherLeader = other[GroupLeader]

        // remove the other from our invited or joinReq lists
        me.set(meLeader.copy(
            invited      = meLeader.invited      filterNot { _ == other.entity },
            joinRequests = meLeader.joinRequests filterNot { _ == other.entity }))

        // remove us from the other entities invited or joinReq lists
        other.set(otherLeader.copy(
            invited      = otherLeader.invited      filterNot { _ == me.entity },
            joinRequests = otherLeader.joinRequests filterNot { _ == me.entity }))
    }


    def accept(me: RichEntity, other: RichEntity) {
        val meLeader = me[GroupLeader]
        var otherLeader = other[GroupLeader]

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
        // clean up my joinrequests
        meLeader.joinRequests
            .filter  { jn => me.world.contains(jn) }
            .map     { jn => me.world.getRich(jn).get }
            .filter  { jn => jn.get[GroupLeader] != None }
            .foreach { jn => 
                jn.set(jn[GroupLeader].copy(
                    invited = jn[GroupLeader].invited filterNot { _ == me.entity })) 
            }
        // new leader for me (swap components)
        me.drop[GroupLeader]
        me.set(GroupMember(other.entity))
        // update invitation of recipient
        other.set(otherLeader.copy(invited = otherLeader.invited filterNot { _ == me.entity }))
    }


    def leaveAsMember(me: RichEntity) {
        // we will need to clean up the old group later on...
        val recipient = me.world.getRich(me[GroupMember].leader).get
        val recLeader = recipient[GroupLeader]

        // get us a new group
        me.drop[GroupMember]
        me.set(GroupLeader())

        // remove us from the old group
        recipient.set(recLeader.copy(members = recLeader.members filterNot { _ == me.entity }))
    }


    def despawnAsMember(meWorld: World, meEntity: Entity, meMember: GroupMember) {
        // we will need to clean up the old group later on...
        val recipient = meWorld.getRich(meMember.leader).get
        val recLeader = recipient[GroupLeader]

        // remove us from the old group
        recipient.set(recLeader.copy(members = recLeader.members filterNot { _ == meEntity }))
    }


    def leaveAsLeader(me: RichEntity) {
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


    def despawnAsLeader(meWorld: World, meEntity: Entity, meLeader: GroupLeader) {
        val oldLeader = meLeader
        val recipient = meWorld.getRich(oldLeader.members(0)).get
        
        // make recipient the new leader...
        recipient.drop[GroupMember]
        recipient.set(oldLeader.copy(members = oldLeader.members filterNot { _ == recipient.entity }))

        // update other team members to have a new leader
        recipient[GroupLeader].members
            .filter  { mem => meWorld.contains(mem) }
            .map     { mem => meWorld.getRich(mem).get }
            .foreach { mem => mem.set(GroupMember(recipient)) }      
    }


    def kick(me: RichEntity, other: RichEntity) {
        val oldLeader = me[GroupLeader]

        // get the former member its own group
        other.drop[GroupMember]
        other.set(GroupLeader())

        // clean up my members
        me.set(oldLeader.copy(members = oldLeader.members filterNot { _ == other.entity }))
    }
}