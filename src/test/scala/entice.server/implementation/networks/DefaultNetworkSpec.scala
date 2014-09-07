/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.networks

import entice.server._
import entice.server.macros._
import entice.server.implementation.loggers.NullLogger
import entice.server.implementation.events._

import entice.protocol._
import entice.protocol.utils._

import akka.actor.{ Actor, ActorRef, Props, ActorSystem }
import akka.testkit.{ TestKit, TestProbe, ImplicitSender }
import akka.io.{ IO, Tcp, TcpPipelineHandler }
import akka.io.TcpPipelineHandler._
import akka.event.NoLogging

import com.typesafe.config.ConfigFactory

import org.scalatest._

import java.net.{ ServerSocket, InetSocketAddress }


class DefaultNetworkSpec
    extends TestKit(ActorSystem(
      "net-spec-sys",
      config = ConfigFactory.parseString("""
        akka {
          loggers = ["akka.event.slf4j.Slf4jLogger"]
          loglevel = WARNING
          log-dead-letters-during-shutdown = off
          logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
          logger-startup-timeout = 30000
        }""")))
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with OneInstancePerTest
    with ImplicitSender {

  import Tcp.{ Connect, Connected, ConnectionClosed, Register }


  /** Component under test */
  trait TestDefaultNetwork
      extends Core
      with DefaultNetwork { self: Logger =>

    lazy val actorSystem = DefaultNetworkSpec.this.system
    lazy val eventBus = new EventBus()

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

      import pipeInit.{ Command, Event }

      // init receiving end
      eventBus.sub[NewSession]
      eventBus.sub[LostSession]
      eventBus.sub[LoginRequest] // not received...
      eventBus.sub[Message]

      // init session1
      val (session1, pipeline) = getClientWithPipeline()
      // we got a connection
      var session1Actor: ActorRef = null
      expectMsgPF() { case Evt(NewSession(sess)) => session1Actor = sess }

      // init session2
      val (session2, _) = getClientWithPipeline()
      // we got another connection
      var session2Actor: ActorRef = null
      expectMsgPF() { case Evt(NewSession(sess)) => session2Actor = sess }

      // c -> s
      session1.send(pipeline, Command(LoginRequest("blubb", "blubb")))
      expectMsgPF() { case msg @ Evt(LoginRequest("blubb", "blubb")) => }

      session2.send(pipeline, Command(LoginRequest("blubb", "blubb")))
      expectMsgPF() { case msg @ Evt(LoginRequest("blubb", "blubb")) => }

      // s -> c
      session1Actor ! LoginRequest("blubb", "blubb")
      session1.expectMsg(Event(LoginRequest("blubb", "blubb")))

      session2Actor ! LoginRequest("blubb", "blubb")
      session2.expectMsg(Event(LoginRequest("blubb", "blubb")))

      // normal kick
      session1Actor ! KickSession
      session1.expectMsgPF() { case c: ConnectionClosed => }
      expectMsgPF() { case Evt(LostSession(_)) => } // lost is backwards reported

      expectNoMsg
      session1.expectNoMsg
      session2.expectNoMsg

      // make sure that clients are kicked during shutdown
      network.shutdown()

      session2.expectMsgPF() { case c: ConnectionClosed => }
      session2.expectNoMsg

      core.shutdown()
    }
  }
}
