/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.utils

import entice.protocol._
import entice.protocol.utils._

import akka.actor._
import akka.io.{ IO, Tcp, TcpPipelineHandler }
import akka.io.TcpPipelineHandler._

import com.typesafe.config.ConfigFactory

import java.net.InetSocketAddress


/** Diagnostic utility to connect to and communicate with the server */
case class ClientUtil(host: String, port: Int) {
  private val actorSystem = ActorSystem("client-sys", config = ConfigFactory.parseString("""
    akka {
      loglevel = INFO
      log-dead-letters = off
    }"""))
  private val clientActor = actorSystem.actorOf(Props(ClientActor(host, port)), "client-actor")

  def send(msg: Message) {
    clientActor ! msg
  }

  def stop() {
    actorSystem.shutdown
  }

  case class ClientActor(host: String, port: Int) extends Actor with ActorLogging {
    import context._
    import Tcp.{ Connect, CommandFailed, Connected, ConnectionClosed, Register }

    IO(Tcp) ! Connect(new InetSocketAddress(host, port))
    val pipeInit = PipelineFactory.getWithLog(log)
    import pipeInit._

    def receive = {
      case CommandFailed(_: Connect) =>
        log.info(s"Client connection failed!")
        become(dead)

      case c @ Connected(remote, _) =>
        log.info(s"Client connected to: ${remote}")
        val connection = sender
        val pipeline = actorSystem.actorOf(TcpPipelineHandler.props(pipeInit, connection, self))
        connection ! Register(pipeline)
        become(active(pipeline))
    }

    def active(pipeline: ActorRef): Receive = {
      case msg: Message        => log.info(s"Client put: ${msg}"); pipeline ! Command(msg)
      case Event(msg)          => log.info(s"Client got: ${msg}")
      case c: ConnectionClosed => log.info(s"Remote closed the connection to client."); become(dead)
    }

    def dead: Receive = {
      case msg => log.info(s"Dead client received message: ${msg}")
    }
  }
}
