/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol.LoginRequest
import entice.server.login.LoginRequestMsg
import entice.protocol.LoginResponse
import entice.protocol.LoginResponse._

import entice.server.login.LoginServiceActor

import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.ImplicitSender
import akka.testkit.TestKit

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration._


 
class LoginServerSpec(system: ActorSystem) extends TestKit(system)
    with WordSpec
    with MustMatchers 
    with BeforeAndAfterAll
    with ImplicitSender {
 

    def this() = this(ActorSystem("LoginServiceSpec"))

    // TODO dep inject all that shit
    val evtStream = system.eventStream
    val login = system.actorOf(Props(classOf[LoginServiceActor], evtStream))


    def pub(obj: Object) { evtStream.publish(obj) }
 

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
 

    // TODO account DAO shit must be mocked!
    "A login server" must {
   
   
        "reply to any login requests with messages of type login response" in {
            pub(LoginRequestMsg(LoginRequest("test@test.de", "password"))(self))
            expectMsgClass(Duration(100, MILLISECONDS), classOf[LoginResponse])
        }


        "reply to invalid login requests with an error code" in {
            // the account will not exists (we need to provide an email anyway!)
            pub(LoginRequestMsg(LoginRequest("test", "test"))(self))
            expectMsg(Duration(100, MILLISECONDS), LoginResponse(Some(ErrorCode.INVALID_CREDENTIALS)))
        }
   
    }
}