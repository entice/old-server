/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol._
import entice.protocol.utils._
import entice.protocol.utils.MessageBus._

import akka.actor._
import akka.io.{ IO, Tcp, TcpPipelineHandler }
import akka.io.TcpPipelineHandler._
import akka.pattern.ask
import akka.event._
import akka.testkit._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import org.scalatest._
import org.scalatest.matchers._

import scala.concurrent._
import scala.concurrent.duration._

import java.net.InetSocketAddress


/**
 * Echos back to the sessions when they publish their messages
 */
class EchoHandler extends Actor with ActorLogging {
    def receive = {
        case MessageEvent(Sender(id, session), data) => 
            log.info("Got the expected data :)")
            session ! data
    }
}


/**
 * Injects our echohandler
 */
trait TestApiSlice extends CoreSlice with ApiSlice {
    import ReactorActor._

    lazy val echo = actorSystem.actorOf(Props[EchoHandler])
    reactor ! Subscribe(echo, classOf[DispatchRequest])
}


class NetworkServerSpec(_system: ActorSystem) extends TestKit(_system)

    // given component
    with CoreSlice
    with TestApiSlice
    with NetSlice

    with WordSpec
    with MustMatchers 
    with BeforeAndAfterAll
    with ImplicitSender {

    import Tcp._


    override lazy val actorSystem = system

    // use the default, meaning bind to any free port ;)
    var localAddr: Option[InetSocketAddress] = None
    
    
    def this() = this(ActorSystem(
        "network-server-spec", 
        config = ConfigFactory.parseString("""
            akka {
              loglevel = WARNING
            }
        """)))


    override def beforeAll {
        import ReactorActor._
        import MetaReactorActor._
        import NetSlice._
        // register a testprobe with the reactor to
        val probe = TestProbe()
        probe.send(reactor, Subscribe(probe.ref, classOf[BindSuccess]))
        probe.send(reactor, Subscribe(probe.ref, classOf[BindFail]))
        // touch the aceptor to create the server...
        // TODO: ensure the server is actually running before testing it ;)
        acceptor ! AddReactor(reactor)
        probe.expectMsgPF(Duration(1000, MILLISECONDS)) {
            case MessageEvent(a, BindSuccess(addr)) => localAddr = Some(addr)
        }
    }


    override def afterAll {
        system.stop(acceptor)
        TestKit.shutdownActorSystem(system)
    }
 

    "An entice network server" must {

        "accept TCP clients" in {
            localAddr must not be(None)
            val probe = TestProbe()

            // try to connect
            probe.send(IO(Tcp), Connect(new InetSocketAddress("localhost", localAddr.get.getPort)))
            probe.expectMsgClass(Duration(1000, MILLISECONDS), classOf[Connected])
            probe.expectNoMsg
        }


        "receive entice.protocol msgs and reply (echo them in this case)" in {
            localAddr must not be(None)
            val probe = TestProbe()

            // try to connect
            probe.send(IO(Tcp), Connect(new InetSocketAddress("localhost", localAddr.get.getPort)))
            probe.expectMsgClass(Duration(1000, MILLISECONDS), classOf[Connected])
            
            // create the serialization stuff
            val connection = probe.sender
            val init = PipelineFactory.getWithLog(NoLogging)
            val pipeline = system.actorOf(TcpPipelineHandler.props(init, connection, probe.ref))
            probe.send(connection, Register(pipeline))

            // deploy test message and wait for it to come back
            probe.send(pipeline, init.Command(DispatchRequest()))
            probe.expectMsgPF(Duration(1000, MILLISECONDS)) {
                case init.Event(data: DispatchRequest) => true
            }
            expectNoMsg
        }
    }
}