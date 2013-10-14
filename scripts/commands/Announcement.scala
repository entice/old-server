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


class Announcement extends Command {

    def info = CommandInfo(
        command = "shout",
        argsHelp ="<some text>" :: Nil,
        generalInfo = "Global server message, with no owner (you should specify your name yourself).",
        usageInfo = "Example: /shout Hi to all!")


    def run(args: List[String], ctx: CommandContext): Option[String] = {
        if (args == Nil) return Some("No message given.")
        ctx.messageBus.publish(
            MessageEvent(
                ctx.sender.session, 
                entice.server.world.Announcement(args.foldLeft("Announcement:") { _ + " " + _ })))
        None
    }
}

new Announcement()