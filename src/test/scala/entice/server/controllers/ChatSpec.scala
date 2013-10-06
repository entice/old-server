/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.controllers

import entice.server._
import entice.server.utils._
import entice.server.world._
import entice.server.systems._
import entice.protocol._
import akka.actor._
import akka.testkit._
import org.scalatest._
import org.scalatest.matchers._


class ChatSpec(_system: ActorSystem) extends TestKit(_system)
    with WordSpec
    with MustMatchers 
    with BeforeAndAfterAll
    with ImplicitSender {

    def this() = this(ActorSystem("chat-spec"))

    def testPub(probe: ActorRef, msg: Message) { 
        MessageBusExtension(_system).publish(MessageEvent(probe, msg)) 
    }

    override def beforeAll {
        val preChat  = _system.actorOf(Props[PreChat])
        val sys      = _system.actorOf(Props[ChatSystem])
    }

    override def afterAll {
        TestKit.shutdownActorSystem(_system)
    }


    "The chat system" must {

        val clients  = ClientRegistryExtension(_system)


        "propagate a chat messages (clients need to be playing)" in {
            // given a few sessions with their client objs and entities
            val session1 = TestProbe(); 
            val session2 = TestProbe(); 
            val session3 = TestProbe(); 

            val client1 = Client(session1.ref, null, null, state = Playing)
            val client2 = Client(session2.ref, null, null, state = Playing)
            val client3 = Client(session3.ref, null, null, state = Playing)

            val world = WorldRegistryExtension(_system).get(client1)

            val ent1 = world.create(new TypedSet[Component]() + Name("chatspecname1")); client1.entity = Some(ent1)
            val ent2 = world.create(new TypedSet[Component]() + Name("chatspecname2")); client2.entity = Some(ent2)
            val ent3 = world.create(new TypedSet[Component]() + Name("chatspecname3")); client3.entity = Some(ent3)

            clients.add(client1)
            clients.add(client2)
            clients.add(client3)

            // chat messages
            testPub(session1.ref, ChatMessage(ent1, "hi"))
            session1.expectMsg(ChatMessage(ent1, "hi"))
            session2.expectMsg(ChatMessage(ent1, "hi"))
            session3.expectMsg(ChatMessage(ent1, "hi"))

            testPub(session2.ref, ChatMessage(ent2, "hi"))
            session1.expectMsg(ChatMessage(ent2, "hi"))
            session2.expectMsg(ChatMessage(ent2, "hi"))
            session3.expectMsg(ChatMessage(ent2, "hi"))

            testPub(session3.ref, ChatMessage(ent3, "hi"))
            session1.expectMsg(ChatMessage(ent3, "hi"))
            session2.expectMsg(ChatMessage(ent3, "hi"))
            session3.expectMsg(ChatMessage(ent3, "hi"))
        }
    }
}