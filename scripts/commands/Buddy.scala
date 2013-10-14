/**
 * For copyright information see the LICENSE document.
 */

import entice.server._
import entice.server.scripting._
import entice.server.utils._
import entice.server.world._
import entice.protocol._
import akka.actor._
import shapeless._
import scala.util._


class Buddy extends Command {

    var buddies: List[ActorRef] = Nil

    def info = CommandInfo(
        command = "buddy",
        argsHelp = 
            " - grouping : Invites all possible entities, and will join any group that invites it." :: 
            " - kill     : Kills all buddy instances." ::
            Nil,
        generalInfo = "Spawns a random character with a specified behaviour (1st arg).",
        usageInfo = "Example: '/buddy grouping' To remove all buddies: '/buddy kill'")


    def run(args: List[String], ctx: CommandContext): Option[String] = {
        if (args == Nil) return Some("No behaviour given.")
        args(0) match {
            case "grouping" => 
                buddies = ctx.actorSystem.actorOf(Props(classOf[GroupingActor], ctx.sender, buddies.length)) :: buddies
                None
            case "kill" => 
                buddies.foreach { ctx.actorSystem.stop(_) }
                None
            case _ => Some(s"Unknown behaviour '${args(0)}'.")
        }
    }


    /**
     * Helping actors that implement the buddy's behaviour:
     */


    class GroupingActor(parent: RichEntity, num: Int) 
        extends System[GroupLeader :: HNil] 
        with Actor 
        with Subscriber 
        with Clients {
        
        val subscriptions = classOf[GroupInvite] :: Nil
        var entity: RichEntity = _

        override def preStart { 
            register
            // set our entity & fake client
            entity = parent.world.create( 
                new TypedSet[Component]()
                    .add(Name(s"Buddy ${num.toString}"))
                    .add(Appearance())
                    .add(parent[Position].copy())
                    .add(GroupLeader()))
            val client = Client(self, null, null, parent.world, Some(entity), Playing)
            clients.add(client)
            // invite all other teamleaders (this is System functionality)
            entities(parent.world) foreach { e => publish(GroupMergeRequest(e)) }
        }

        override def postStop {
            // clean up
            parent.world.remove(entity) // remove my entity
            unloadFrom(parent.world)    // remove my system
            clients.remove(self)
        }

        def receive = {
            // if someone invites us, accept it instantly
            case MessageEvent(_, GroupInvite(e, entity)) => publish(GroupMergeRequest(e))
            // ignore all other messages
            case _ => 
        }
    }
}

new Buddy()


