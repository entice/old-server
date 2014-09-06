/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.networks

import entice.server._
import entice.server.macros._
import entice.server.implementation.cores.StaticCore
import entice.server.implementation.loggers.NullLogger
import entice.server.implementation.events._

import entice.protocol._
import entice.protocol.utils._

import akka.actor._
import akka.testkit._
import akka.io.{ IO, Tcp, TcpPipelineHandler }
import akka.io.TcpPipelineHandler._
import akka.event._

import org.scalatest._

import java.net.{ ServerSocket, InetSocketAddress }


class DefaultNetworkSpec
    extends WordSpec
    with Matchers
    with OneInstancePerTest {

  import Tcp.{ Connect, Connected, ConnectionClosed, Register }


  /** Component under test */
  trait TestDefaultNetwork
      extends StaticCore
      with DefaultNetwork { self: Logger =>

    lazy val serverHost = "127.0.0.1"
    lazy val serverPort = {
      val server = new ServerSocket(0)
      val port = server.getLocalPort()
      server.close()
      port
    }

    // for clients
    val pipeInit = PipelineFactory.getWithLog(NoLogging)

    /** Create a new already connected client */
    def getClientWithPipeline(): (TestProbe, ActorRef) = {
      implicit val sys = actorSystem

      val clientProbe = TestProbe()
      // connect...
      clientProbe.send(IO(Tcp), Connect(new InetSocketAddress(serverHost, serverPort)))
      clientProbe.expectMsgClass(classOf[Connected])
      // build the pipeline...
      val connection = clientProbe.sender
      val pipeline = actorSystem.actorOf(TcpPipelineHandler.props(pipeInit, connection, clientProbe.ref))
      clientProbe.send(connection, Register(pipeline))
      // done...
      (clientProbe, pipeline)
    }
  }


  "An entice network layer" should {

    "receive messages and report them" in new TestDefaultNetwork with NullLogger {

      core.init()
      network.init()

      implicit val sys = actorSystem
      import pipeInit.{ Command, Event }

      // init receiving end
      val serverProbe = TestProbe()
      implicit val act = serverProbe.ref
      eventBus.sub[NewSession]
      eventBus.sub[LostSession]
      eventBus.sub[LoginRequest]
      eventBus.sub[Message]

      // init sending end
      val (clientProbe, pipeline) = getClientWithPipeline()

      // we got a connection
      serverProbe.expectMsgPF() { case Evt(NewSession(_)) => }

      // c -> s
      clientProbe.send(pipeline, Command(LoginRequest("blubb", "blubb")))
      serverProbe.expectMsgPF() {
        case msg @ Evt(LoginRequest(_, _)) =>
          // echo
          serverProbe.send(msg.sender, msg.message)
          // then kick
          serverProbe.send(msg.sender, KickSession)
      }
      // s -> c
      clientProbe.expectMsg(Event(LoginRequest("blubb", "blubb")))

      serverProbe.expectMsgPF() { case Evt(LostSession(_)) => }
      clientProbe.expectMsgPF() { case c: ConnectionClosed => }

      serverProbe.expectNoMsg
      clientProbe.expectNoMsg

      // now make sure that clients are kicked during shutdown
      // connect again...
      val (otherClientProbe, _) = getClientWithPipeline()

      network.shutdown()

      otherClientProbe.expectMsgPF() { case c: ConnectionClosed => }
      otherClientProbe.expectNoMsg

      core.shutdown()
    }
  }
}
