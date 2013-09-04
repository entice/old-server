/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol._
import entice.protocol.utils._
import entice.protocol.utils.MessageBus._

import entice.server.login._

import akka.actor._
import akka.io.{ IO, Tcp, TcpPipelineHandler }
import akka.io.TcpPipelineHandler._
import akka.event._
import akka.testkit._

import com.softwaremill.macwire.Macwire

import org.scalatest._
import org.scalatest.matchers._

import scala.concurrent.duration._

import java.net.InetSocketAddress


/**
 * Echos back to the sessions when they publish their messages
 */
class EchoHandler extends Actor with ActorLogging {
    def receive = {
        case MessageEvent(session, data) => 
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
    reactor ! Subscribe(echo, classOf[LoginRequest])
}   


/**
 * A testserver that we can communicate with
 */
case class TestServer(port: Int) extends CoreSlice with TestApiSlice with NetSlice {
    override lazy val localAddress = new InetSocketAddress(port)
    start
}


class NetworkServerSpec(_system: ActorSystem) extends TestKit(_system)
    with WordSpec
    with MustMatchers 
    with BeforeAndAfterAll
    with ImplicitSender {

    import Tcp._
    

    def this() = this(ActorSystem("LoginServerSpec"))

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
 

    "An entice network server" must {

        "be able to accept TCP clients" in {
            // given
            val server = TestServer(8111)
            val probe = TestProbe()
            // try to connect
            probe.send(IO(Tcp), Connect(new InetSocketAddress("localhost", 8111)))
            probe.expectMsgClass(Duration(100, MILLISECONDS), classOf[Connected])
        }


        "receive entice.protocol msgs and reply (echo them in this case)" in {
            // given
            val server = TestServer(8112)
            val probe = TestProbe()
            // try to connect
            probe.send(IO(Tcp), Connect(new InetSocketAddress("localhost", 8112)))
            probe.expectMsgClass(Duration(100, MILLISECONDS), classOf[Connected])
            
            // create the serialization stuff
            val connection = probe.sender
            val init = PipelineFactory.getWithLog(NoLogging)
            val pipeline = system.actorOf(TcpPipelineHandler.props(init, connection, probe.ref))
            probe.send(connection, Register(pipeline))

            // deploy test message and wait for it to come back
            probe.send(pipeline, init.Command(LoginRequest("test", "test")))
            probe.expectMsgPF(Duration(1000, MILLISECONDS)) {
                case init.Event(data: LoginRequest) => true
            }
            expectNoMsg
        }
    }
}