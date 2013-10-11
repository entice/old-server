/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.controllers

import entice.server._, Net._
import entice.server.test._
import entice.server.utils._
import entice.server.database._
import entice.protocol._
import akka.actor._
import akka.testkit._
import com.typesafe.config.ConfigFactory
import com.mongodb.casbah.commons.Imports._
import org.scalatest._
import org.scalatest.matchers._


class CharCreateSpec extends TestKit(ActorSystem(
    "char-create-spec", 
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
    val charCreate = TestActorRef[CharCreate]

    // given
    val clients = ClientRegistryExtension(system)
    val session1 = TestProbe()
    val session2 = TestProbe()
    val client1 = Client(session1.ref, Account(email = "charcreatespec1@entice.org", password = "test"), Map())
    val client2 = Client(session2.ref, Account(email = "charcreatespec2@entice.org", password = "test"), Map())
    val char = Character(accountId = new ObjectId(), name = Name("char-create-spec-test2"))


    override def beforeAll { 
        // register the test clients
        clients.add(client1)
        clients.add(client2)

        // register a db char that we can test for
        Character.create(char)
    }
    
    override def afterAll  { 
        // db cleanup
        Character.delete(char)

        TestKit.shutdownActorSystem(system) 
    }


    "A char-create controller" must {


        "create chars when receiving a valid request" in {
            fakePub(charCreate, session1.ref, CharCreateRequest(CharacterView(Name("char-create-spec-test1"), Appearance())))
            session1.expectMsgPF() {
                case CharCreateSuccess(Entity(_)) => true
            }
            session1.expectNoMsg

            // cleanup
            Character.deleteByName(Name("char-create-spec-test1"))
        }


        "reply with an error message when the name is already taken" in {
            // given an existing char
            fakePub(charCreate, session2.ref, CharCreateRequest(CharacterView(Name("char-create-spec-test2"), Appearance())))
            session2.expectMsgClass(classOf[CharCreateFail])
            session2.expectNoMsg
        }


        "detect hacks" in {
            val noClient = TestProbe()
            fakePub(charCreate, noClient.ref, CharCreateRequest(CharacterView(Name("something"), Appearance())))
            noClient.expectMsgClass(classOf[CharCreateFail])
            noClient.expectMsg(Kick)
        }
    }
}