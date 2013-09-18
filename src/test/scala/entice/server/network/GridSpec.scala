/**
 * For copyright information see the LICENSE document.
 */

package entice.server.network

import akka.actor._
import akka.io.IO
import java.net.InetSocketAddress
import org.scalatest._
import org.scalatest.matchers._


class GridSpec(_system: ActorSystem) extends TestKit(_system) 
    with WordSpec
    with MustMatchers 
    with BeforeAndAfterAll
    with ImplicitSender {

    def this() = this(ActorSystem("grid-spec-sys"))

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

    "An entice network grid" must {


        "bind on a free port" in {
            IO(Grid) ! Bind(new InetSocketAddress(0))
            expectMessageClass(classOf[BindSuccess])

            probe.expectNoMsg
            expectNoMsg
        }


        "accept connections and report them" in {
            // given
            IO(Grid) ! Bind(new InetSocketAddress(0))
            var addr: Option[InetSocketAddress] = None
            expectMessagePF() {
                case BindSuccess(a) => addr = Some(a)
            }

            // when
            addr must not be(None)
            val probe = TestProbe()
            probe.send(IO(Tcp), Connect(new InetSocketAddress("localhost", addr.get.getPort)))
            probe.expectMsgClass(classOf[Connected])

            // must
            expectMessageClass(classOf[NewSession])

            probe.expectNoMsg
            expectNoMsg
        }


        "receive messages and report them" in {
            // given
            IO(Grid) ! Bind(new InetSocketAddress(0))
            var addr: Option[InetSocketAddress] = None
            expectMessagePF() {
                case BindSuccess(a) => addr = Some(a)
            }

            // when
            addr must not be(None)
            val probe = TestProbe()
            probe.send(IO(Tcp), Connect(new InetSocketAddress("localhost", addr.get.getPort)))
            probe.expectMsgClass(classOf[Connected])

            // must
            expectMessageClass(classOf[NewSession])

            // now given
            val connection = probe.sender
            val init = PipelineFactory.getWithLog(NoLogging)
            val pipeline = _system.actorOf(TcpPipelineHandler.props(init, connection, probe.ref))
            probe.send(connection, Register(pipeline))

            // when deploying a test message
            probe.send(pipeline, init.Command(DispatchRequest()))
            // we receive it, and echo it
            expectMsgPF() {
                case NewMessage(m: DispatchRequest) => sender ! m // echo
            }
            // and the probe receives the echo
            probe.expectMsgPF() {
                case init.Event(m: DispatchRequest) => true
            }

            probe.expectNoMsg
            expectNoMsg
        }
    }
}
