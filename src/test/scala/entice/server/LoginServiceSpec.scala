/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol.LoginRequest
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


 
class LoginServiceSpec(_system: ActorSystem) extends TestKit(_system)
    with WordSpec
    with MustMatchers 
    with BeforeAndAfterAll
    with ImplicitSender {
 

    def this() = this(ActorSystem("LoginServiceSpec"))
 

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
 

    "A login service actor" must {
   
        "reply to login requests with login responses" in {
            val echo = system.actorOf(Props[LoginServiceActor])
            echo ! LoginRequest("test@test.de", "password")
            expectMsgClass(classOf[LoginResponse])
        }


        "reply to invalid login requests with an error code" in {
            val echo = system.actorOf(Props[LoginServiceActor])
            echo ! LoginRequest("test", "test")
            expectMsg(LoginResponse(Some(ErrorCode.INVALID_CREDENTIALS)))
        }
   
    }
}