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

import org.scalatest._
import org.scalatest.matchers._

import scala.concurrent.duration._


 
class LoginServerSpec(system: ActorSystem) extends TestKit(system)

    // given components
    with CoreSlice
    with LoginApiSlice

    with WordSpec
    with MustMatchers 
    with BeforeAndAfterAll
    with ImplicitSender {

    override lazy val actorSystem = system

 
    def this() = this(ActorSystem("login-server-spec"))


    def testPub(probe: ActorRef, msg: Message) { 
        messageBus.publish(MessageEvent(probe, msg)) 
    }
 

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
 

    // TODO account DAO shit must be mocked!
    "A login server" must {
   
   
        "reply to any valid login requests with a login success" in {
            val probe = TestProbe()(system)
            testPub(probe.ref, LoginRequest("test@test.de", "password")) // TODO: until we mock the DAO, this will be good
            probe.expectMsgClass(classOf[LoginSuccess])
            probe.expectNoMsg
        }


        "reply to any invalid login requests with an error code" in {
            val probe = TestProbe()(system)
            testPub(probe.ref, LoginRequest("test", "test"))
            probe.expectMsgPF() {
                case LoginFail(errorMsg) if errorMsg != "" => true
            }
            probe.expectNoMsg
        }
        

        "reply to dispatch requests with a message of type dispatch response" in {
            // the account will not exists (we need to provide an email anyway!)
            val probe = TestProbe()(system)
            testPub(probe.ref, DispatchRequest())
            probe.expectMsgClass(classOf[DispatchResponse])
            probe.expectNoMsg
        }
    }
}