/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol._
import entice.protocol.utils._
import entice.protocol.utils.MessageBus._

import entice.server.login._

import akka.actor._
import akka.testkit._
import akka.io.{ IO, Tcp, TcpPipelineHandler }
import com.typesafe.config.ConfigFactory

import org.scalatest._
import org.scalatest.matchers._

import scala.concurrent.duration._

import java.net.InetSocketAddress

 
class LoginServerSpec(_system: ActorSystem) extends TestKit(_system)

    // given components
    with CoreSlice
    with LoginApiSlice

    with WordSpec
    with MustMatchers 
    with BeforeAndAfterAll
    with ImplicitSender {

    import Tcp.{ Connect, Connected }
    import SessionActor._
    import ActorSlice._

    override lazy val actorSystem = _system
    override lazy val serverActor = self

 
     def this() = this(ActorSystem(
        "login-server-spec", 
        config = ConfigFactory.parseString("""
            akka {
              loglevel = WARNING
            }
        """)))


    def testPub(id: UUID, probe: ActorRef, msg: Message) { 
        messageBus.publish(MessageEvent(Sender(id, probe), msg)) 
    }
 

    override def afterAll {
        TestKit.shutdownActorSystem(_system)
    }
 

    // TODO account DAO shit must be mocked!
    "A login server" must {
   
   
        "accept clients with a valid login request, and reply with a login success" in {
            val probe = TestProbe()
            val id = UUID()
            testPub(id, probe.ref, LoginRequest("test@test.de", "password")) // TODO: until we mock the DAO, this will be good
            probe.expectMsgClass(classOf[Reactor])
            probe.expectMsgClass(classOf[LoginSuccess])
            probe.expectNoMsg
        }


        "reply to any invalid login requests with an error code" in {
            val probe = TestProbe()
            val id = UUID()
            testPub(id, probe.ref, LoginRequest("test", "test"))
            probe.expectMsgPF() {
                case LoginFail(errorMsg) if errorMsg != "" => true
            }
            probe.expectNoMsg
        }
        

        "reply to dispatch requests with a valid dispatch response" in {
            // we will inject the "Client" first so we dont need to login
            val probe = TestProbe() // the mocked session actor of the client
            val id = UUID()
            clientRegistry.add(Client(id, probe.ref))
            // we can now request a game server...
            testPub(id, probe.ref, DispatchRequest())

            // it will notify us (the server) that it wants us to notfy the gs
            expectMsgPF() {
                case SendTo(srv, msg: AddPlayer) =>
                    srv ! msg
            }
            // the gs will send us something...
            expectMsgPF() {
                case msg: WaitingForPlayer =>
                    testPub(id, self, msg)
            }

            // we can now extract the actual gs from the response
            var gsAddr: Option[InetSocketAddress] = None
            probe.expectMsgPF() {
                case DispatchResponse(host, port, key) => 
                    gsAddr = Some(new InetSocketAddress(host, port))
            }
            probe.expectNoMsg
            // testing the connection to GS
            gsAddr must not be(None)

            // try to connect
            probe.send(IO(Tcp), Connect(gsAddr.get))
            probe.expectMsgClass(classOf[Connected])
            probe.expectNoMsg
        }
    }
}