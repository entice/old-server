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
 
    def this() = this(ActorSystem("LoginServerSpec"))

    // override existing actorsystem
    override lazy val actorSystem = system


    def testPub(probe: ActorRef, msg: Message) { 
        messageBus.publish(MessageEvent(probe, msg)) 
    }
 

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
 

    // TODO account DAO shit must be mocked!
    "A login server" must {
   
   
        "reply to any login requests with a message of type login response" in {
            val probe = TestProbe()(system)
            testPub(probe.ref, LoginRequest("test@test.de", "password"))
            probe.expectMsgClass(classOf[LoginResponse])
            probe.expectNoMsg
        }


        "reply to invalid login requests with an error code" in {
            // the account will not exists (we need to provide an email anyway!)
            val probe = TestProbe()(system)
            testPub(probe.ref, LoginRequest("test", "test"))
            probe.expectMsgPF() {
                case LoginResponse(errorMsg) if errorMsg != "" => true
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