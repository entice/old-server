/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol._
import entice.protocol.utils._
import akka.actor._
import akka.testkit._
import akka.io.{ IO, Tcp, TcpPipelineHandler }
import akka.io.TcpPipelineHandler._
import akka.event._
import com.typesafe.config.ConfigFactory
import java.net.InetSocketAddress
import org.scalatest._
import org.scalatest.matchers._


class NetSpec(_system: ActorSystem) extends TestKit(_system) 
    with WordSpec
    with MustMatchers 
    with BeforeAndAfterAll
    with ImplicitSender {

    import Net._
    import Tcp.{ Connect, Connected, Register }

    def this() = this(ActorSystem(
        "net-spec-sys", 
        config = ConfigFactory.parseString("""
            akka {
              loglevel = WARNING
            }
        """)))

    implicit val actorSystem = _system

    override def afterAll {
        TestKit.shutdownActorSystem(_system)
    }

    /**
     * A short hint on the following tests:
     * Because the network systems has several states (of connection),
     * we need to reproduce certain states before testing the next ones.
     * This is why the code is repreated very often.
     * We could reduce this to the last test only, but it doesnt give us the
     * simplicity of checking what has gone wrong.
     */

    "An entice network extension" must {


        "bind on a free port" in {
            IO(Net) ! Start(new InetSocketAddress(0))
            expectMsgClass(classOf[BindSuccess])
        }


        "accept connections and report them" in {
            // given
            IO(Net) ! Start(new InetSocketAddress(0))
            var addr: Option[InetSocketAddress] = None
            expectMsgPF() {
                case BindSuccess(a) => addr = Some(a)
            }

            // when
            addr must not be(None)
            val probe = TestProbe()
            probe.send(IO(Tcp), Connect(new InetSocketAddress("localhost", addr.get.getPort)))
            probe.expectMsgClass(classOf[Connected])

            // must
            expectMsgClass(classOf[NewSession])

            probe.expectNoMsg
        }


        "receive messages and report them" in {
            // given
            IO(Net) ! Start(new InetSocketAddress(0))
            var addr: Option[InetSocketAddress] = None
            expectMsgPF() {
                case BindSuccess(a) => addr = Some(a)
            }

            // when
            addr must not be(None)
            val probe = TestProbe()
            probe.send(IO(Tcp), Connect(new InetSocketAddress("localhost", addr.get.getPort)))
            probe.expectMsgClass(classOf[Connected])

            // must
            expectMsgClass(classOf[NewSession])

            // now given
            val connection = probe.sender
            val init = PipelineFactory.getWithLog(NoLogging)
            val pipeline = _system.actorOf(TcpPipelineHandler.props(init, connection, probe.ref))
            probe.send(connection, Register(pipeline))

            // when deploying a test message
            probe.send(pipeline, init.Command(LoginRequest("blubb", "blubb")))
            // we receive it, and echo it
            expectMsgPF() {
                case NewMessage(m: LoginRequest) => lastSender ! m // echo
            }
            // and the probe receives the echo
            probe.expectMsgPF() {
                case init.Event(m: LoginRequest) => true
            }

            probe.expectNoMsg
        }
    }
}
