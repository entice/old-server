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
                buddies = ctx.actorSystem.actorOf(Props(new GroupingActor(ctx.sender.entity.get, buddies.length))) :: buddies
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


    class GroupingActor(
        parent: RichEntity, 
        num: Int) extends Actor with Subscriber with Clients {
        
        val subscriptions = classOf[GroupInvite] :: Nil
        var entity: RichEntity = _

        override def preStart { 
            register
            // set our entity
            entity = parent.world.create( 
                new TypedSet[Component]()
                    .add(Name(s"Buddy ${num.toString}"))
                    .add(Appearance())
                    .add(parent[Position].copy())
                    .add(GroupLeader()))
            // invite all other teamleaders
            parent.world.dump.keySet
                .map     { e => parent.world.getRich(e).get }
                .filter  { e => entity != e }
                .filter  { e => entity.get[GroupLeader] != None }
                .foreach { e => publish(GroupInvite(entity, e)) }
        }

        override def postStop {
            // clean up
            parent.world.remove(entity)
        }

        def receive = {
            // if someone invites us, accept it instantly
            case MessageEvent(_, GroupInvite(e1, e2)) if e2 == entity =>
                publish(GroupAccept(entity, e1))
            // ignore all other messages
            case _ => 
        }
    }
}

new Buddy()


