/**
 * For copyright information see the LICENSE document.
 */

package entice.server.scripting

import entice.server._
import entice.server.utils._
import akka.actor._


case class CommandInfo(
    command: String,
    argsHelp: List[String], // each arg gets one string explaining it (in order)
    generalInfo: String,
    usageInfo: String)


case class CommandContext(
    actorSystem: ActorSystem,
    messageBus: MessageBus,
    sender: Client)


/**
 * Will be called from the GW client as follows:
 * /commandname arg1 arg2 "long arg 3"
 * and will be translated to:
 * (call to the command 'commandname' if exists) with args: List("arg1", "arg2", "long arg 3") 
 */
trait Command { 
    def info: CommandInfo
    def run(args: List[String], ctx: CommandContext): Option[String] // exits with some string in case of error, else none
}