/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.controllers

import entice.server._
import entice.server.test._
import entice.server.utils._
import entice.server.database._
import entice.protocol._
import akka.actor._
import akka.testkit._
import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.scalatest.matchers._


class LoginSpec extends TestKit(ActorSystem(
    "login-spec", 
    config = ConfigFactory.parseString("""
        akka {
          loglevel = WARNING
        }
    """)))

    with WordSpec
    with MustMatchers 
    with BeforeAndAfterAll
    with OneInstancePerTest
    with ImplicitSender {


    // actor under test
    val login = TestActorRef[Login]

    // given
    val clients = ClientRegistryExtension(system)
    val acc1 = Account(email = "loginspec1@entice.org", password = "test")
    val acc2 = Account(email = "loginspec2@entice.org", password = "test")
    val noacc = Account(email = "nonexisting@entice.org", password = "test")


    override def beforeAll { 
        // given an existing acc
        Account.create(acc1)
        Account.create(acc2)

        // given a nonexisting acc
        Account.create(noacc)
        Account.delete(noacc)
    }

    override def afterAll {
        Account.delete(acc1)
        Account.delete(acc2)

        TestKit.shutdownActorSystem(system)
    }


    "A login controller" must {       


        "accept clients with a valid login request, and reply with a login success" in {
            val probe = TestProbe()
            fakePub(login, probe.ref, LoginRequest("loginspec1@entice.org", "test"))
            probe.expectMsgClass(classOf[LoginSuccess])
            probe.expectNoMsg
        }


        "detect multi-account logins, and reply with an error" in {
            val probe = TestProbe()
            clients.add(Client(self, Account(email = "loginspec2@entice.org", password = "test"), null, null))
            fakePub(login, probe.ref, LoginRequest("loginspec2@entice.org", "test"))
            probe.expectMsgClass(classOf[Failure])
            probe.expectNoMsg
        }


        "reply to any invalid login requests with an error" in {
            val probe = TestProbe()
            fakePub(login, probe.ref, LoginRequest("nonexisting@entice.org", "test"))
            probe.expectMsgClass(classOf[Failure])
            probe.expectNoMsg
        }

    }
}