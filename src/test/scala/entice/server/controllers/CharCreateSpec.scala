/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.controllers

import entice.server._, Net._
import entice.server.utils._
import entice.server.database._
import entice.protocol._
import akka.actor._
import akka.testkit._
import com.mongodb.casbah.commons.Imports._
import org.scalatest._
import org.scalatest.matchers._


class CharCreateSpec(_system: ActorSystem) extends TestKit(_system)
    with WordSpec
    with MustMatchers 
    with BeforeAndAfterAll
    with ImplicitSender {

    def this() = this(ActorSystem("char-create-spec"))

    def testPub(probe: ActorRef, msg: Message) { 
        MessageBusExtension(_system).publish(MessageEvent(probe, msg)) 
    }

    override def afterAll {
        TestKit.shutdownActorSystem(_system)
    }


    "A char-create controller" must {

        val play = _system.actorOf(Props[CharCreate])
        val clients = ClientRegistryExtension(_system)

        // given our client
        val session1 = TestProbe()
        val session2 = TestProbe()
        val client1 = Client(session1.ref, Account(email = "charcreatespec1@entice.org", password = "test"), Map())
        val client2 = Client(session2.ref, Account(email = "charcreatespec2@entice.org", password = "test"), Map())
        
        clients.add(client1)
        clients.add(client2)


        "create chars when receiving a valid request" in {
            testPub(session1.ref, CharCreateRequest(CharacterView(Name("char-create-spec-test1"), Appearance())))
            session1.expectMsgPF() {
                case CharCreateSuccess(Entity(_)) => true
            }
            session1.expectNoMsg

            // cleanup
            Character.deleteByName(Name("char-create-spec-test1"))
        }


        "reply with an error message when the name is already taken" in {
            // given an existing char
            val char = Character(accountId = new ObjectId(), name = Name("char-create-spec-test2"))
            Character.create(char)

            testPub(session2.ref, CharCreateRequest(CharacterView(Name("char-create-spec-test2"), Appearance())))
            session2.expectMsgClass(classOf[CharCreateFail])
            session2.expectNoMsg

            // cleanup
            Character.delete(char)
        }


        "detect hacks" in {
            val noClient = TestProbe()
            testPub(noClient.ref, CharCreateRequest(CharacterView(Name("something"), Appearance())))
            noClient.expectMsgClass(classOf[CharCreateFail])
            noClient.expectMsg(Kick)
        }
    }
}