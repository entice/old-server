/**
 * For copyright information see the LICENSE document.
 */

import entice.server._
import entice.server.scripting._
import entice.server.utils._
import entice.server.world._
import entice.protocol._
import akka.actor._
import scala.util._


class Emote extends Command {

    def info = CommandInfo(
        command = "perform",
        argsHelp ="<some emote>" :: Nil,
        generalInfo = "Lets your character perform an emote.",
        usageInfo = "Example: /perform dance")


    def run(args: List[String], ctx: CommandContext): Option[String] = {
        if (args.head == Nil) return Some("No emote given.")
        Try(Animations.withName(args.head)) match {
            case res: Success[_] => 
                ctx.messageBus.publish(MessageEvent(ctx.sender.session, Animate(ctx.sender.entity.get, args.head)))
                None
            case res: Failure[_] =>
                Some("Emote does not exist.")
        }
    }
}

new Emote()