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


class PreGroup extends Actor with Subscriber with Clients {

    val subscriptions = classOf[GroupMergeRequest] :: classOf[GroupKickRequest] :: Nil
    override def preStart { register }


    def receive = {

        case MessageEvent(session, GroupMergeRequest(target)) =>
            clients.get(session)  match {
                
                // invite or accept
                case Some(client @ Client(_, _, _, world, entity, state))
                    if (state == Playing 
                    &&  entity != None
                    &&  entity.get.get[GroupLeader] != None
                    &&  entity.get.entity != target
                    &&  world.contains(target) 
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

                        if (entity.get[GroupLeader].joinRequests.contains(recipient.entity)) {
                            publish(GroupAccept(entity.get, recipient))
                        }
                        else {
                            publish(GroupInvite(entity.get, recipient))
                        }
                    }


                case _ =>
                    session ! Failure("Not logged in, or not playing, or no groupleader or invalid target.")
                    // TODO: Is this necessary: session ! Kick
            }


        case MessageEvent(session, GroupKickRequest(target)) =>
            clients.get(session)  match {       

                // if the client is a member, the target must be us
                case Some(client @ Client(_, _, _, world, entity, state))
                    if (state == Playing 
                    &&  entity != None
                    && (entity.get.get[GroupLeader] != None
                    ||  entity.get.get[GroupMember] != None)
                    &&  entity.get.entity == target) =>

                    publish(GroupLeave(entity.get))


                // if the client is leader, and the target is NOT us, and the target is part of our group
                case Some(client @ Client(_, _, _, world, entity, state))
                    if (state == Playing 
                    &&  entity != None
                    &&  entity.get.get[GroupMember] == None
                    &&  entity.get.get[GroupLeader] != None
                    &&  entity.get.entity != target
                    &&  world.contains(target) 
                    &&  entity.get[GroupLeader].members != Nil 
                    &&  entity.get[GroupLeader].members.contains(target) 
                    &&  world.getComps(target).get.contains[GroupMember]) =>

                    publish(GroupKick(entity.get, world.getRich(target).get))


                // if the client is leader, and the target is NOT us, and the target is NOT part of our group
                // this is only valid if the target is in our invited or joinrequest lists
                case Some(client @ Client(_, _, _, world, entity, state))
                    if (state == Playing 
                    &&  entity != None
                    &&  entity.get.get[GroupMember] == None
                    &&  entity.get.get[GroupLeader] != None
                    &&  entity.get.entity != target
                    &&  world.contains(target)
                    && (entity.get[GroupLeader].invited.contains(target)
                    ||  entity.get[GroupLeader].joinRequests.contains(target))
                    &&  world.getComps(target).get.contains[GroupLeader]) =>

                    publish(GroupDecline(entity.get, world.getRich(target).get))
 

                case _ =>
                    session ! Failure("Not logged in, or not playing, or no groupleader or invalid target.")
                    // TODO: Is this necessary: session ! Kick
            }
    }
}