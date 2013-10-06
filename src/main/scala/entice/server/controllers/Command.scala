/**
 * For copyright information see the LICENSE document.
 */

package entice.server.controllers

import entice.server._, Net._
import entice.server.world._
import entice.server.utils._
import entice.server.scripting._
import entice.protocol._
import com.twitter.util._
import akka.actor.{ Actor, ActorRef, ActorLogging, ActorSystem, Props }
import java.io._


class Command extends Actor with ActorLogging with Subscriber with Clients with Configurable {

    val subscriptions = classOf[ChatCommand] :: Nil
    override def preStart { register }

    var scripts: Map[String, scripting.Command] = retrieveScripts
    val scriptContext = CommandContext(context.system, messageBus, _: Client)


    def receive = {
        case MessageEvent(session, ChatCommand(cmd, args)) =>
            clients.get(session)  match {
                case Some(client) if client.state == Playing =>
                    
                    if (cmd == "helpme") {
                        session ! ServerMessage("Available commands:")
                        session ! ServerMessage(" - (built-in) helpme")
                        session ! ServerMessage(" - (built-in) info <command-name>")
                        session ! ServerMessage(" - (built-in) load <path/to/file>")
                        scripts.foreach { scr => session ! ServerMessage(s" - ${scr}") }
                    }
                    
                    if (cmd == "info" && args.head != Nil) {
                        scripts.get(args.head) match {
                            case Some(script) =>
                                session ! ServerMessage(s"Command '${cmd}' does:")
                                session ! ServerMessage(script.info.generalInfo)
                                session ! ServerMessage(s"Command '${cmd}' takes:")
                                script.info.argsHelp foreach { a => session ! ServerMessage(s" - ${a}") }
                                session ! ServerMessage(s"Command '${cmd}' usage:")
                                session ! ServerMessage(script.info.usageInfo)
                            case None => session ! ServerMessage("No such command available.")
                        }
                    }

                    else if (cmd == "load") {
                        // TODO: load smth on the fly
                        session ! ServerMessage("Not yet implemented.")
                    }
                    
                    else if (scripts.contains(cmd)) {
                        log.debug(s"\nRunning script for command '${cmd}'...")
                        scripts(cmd).run(args, scriptContext(client)) match {
                            case Some(errormsg) => session ! ServerMessage(errormsg)
                            case None =>
                        }
                    }
                case _ =>
                    session ! Kick
            }
    }


    def retrieveScripts = {
        val scriptFiles = new File(config.commandScripts).listFiles()
        var result = Map[String, scripting.Command]()

        for (scriptFile <- scriptFiles) {
            val script = (new Eval)[scripting.Command](scriptFile)
            log.info(s"Loaded script for command '${script.info.command}'.")
            result = result + (script.info.command -> script)
        }

        result
    }
}