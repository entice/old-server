/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.world.systems

import entice.server._
import entice.server.test._
import entice.server.utils._
import entice.server.world._
import entice.server.controllers._
import entice.protocol._
import akka.actor._
import akka.testkit._
import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.scalatest.matchers._


class ChatSystemSpec extends TestKit(ActorSystem(
    "chat-spec", 
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


    // actors under test
    val chat = TestActorRef[PreChat]
    val chatSys = TestActorRef[ChatSystem]

    // given
    val clients  = ClientRegistryExtension(system)
    val worlds = WorldRegistryExtension(system)


    override def afterAll  { TestKit.shutdownActorSystem(system) }


    "The chat system" must {


        "propagate chat messages on the 'all' channel (clients need to be playing)" in {
            // given a few sessions with their client objs and entities
            val session1 = TestProbe(); 
            val session2 = TestProbe(); 
            val session3 = TestProbe(); 

            val client1 = Client(session1.ref, null, null, worlds.default, state = Playing)
            val client2 = Client(session2.ref, null, null, worlds.default, state = Playing)
            val client3 = Client(session3.ref, null, null, worlds.default, state = Playing)

            val world = worlds.default

            val ent1 = world.create(new TypedSet[Component]() + Name("chatspecname1")); client1.entity = Some(ent1)
            val ent2 = world.create(new TypedSet[Component]() + Name("chatspecname2")); client2.entity = Some(ent2)
            val ent3 = world.create(new TypedSet[Component]() + Name("chatspecname3")); client3.entity = Some(ent3)

            clients.add(client1)
            clients.add(client2)
            clients.add(client3)

            // chat messages
            fakePub(chat, session1.ref, ChatMessage(ent1, "hi1", ChatChannels.All.toString))
            session1.expectMsg(ChatMessage(ent1, "hi1", ChatChannels.All.toString))
            session2.expectMsg(ChatMessage(ent1, "hi1", ChatChannels.All.toString))
            session3.expectMsg(ChatMessage(ent1, "hi1", ChatChannels.All.toString))

            fakePub(chat, session2.ref, ChatMessage(ent2, "hi2", ChatChannels.All.toString))
            session1.expectMsg(ChatMessage(ent2, "hi2", ChatChannels.All.toString))
            session2.expectMsg(ChatMessage(ent2, "hi2", ChatChannels.All.toString))
            session3.expectMsg(ChatMessage(ent2, "hi2", ChatChannels.All.toString))

            fakePub(chat, session3.ref, ChatMessage(ent3, "hi3", ChatChannels.All.toString))
            session1.expectMsg(ChatMessage(ent3, "hi3", ChatChannels.All.toString))
            session2.expectMsg(ChatMessage(ent3, "hi3", ChatChannels.All.toString))
            session3.expectMsg(ChatMessage(ent3, "hi3", ChatChannels.All.toString))
        }

        "propagate chat messages on the 'group' channel (clients need to be playing)" in {
            // given a few sessions with their client objs and entities
            val sessionE1 = TestProbe(); 
            val sessionE2 = TestProbe(); 
            val sessionE3 = TestProbe(); 
            val sessionM11 = TestProbe(); 
            val sessionM12 = TestProbe(); 
            val sessionM21 = TestProbe();

            val clientE1 = Client(sessionE1.ref, null, null, worlds.default, state = Playing)
            val clientE2 = Client(sessionE2.ref, null, null, worlds.default, state = Playing)
            val clientE3 = Client(sessionE3.ref, null, null, worlds.default, state = Playing)
            val clientM11 = Client(sessionM11.ref, null, null, worlds.default, state = Playing)
            val clientM12 = Client(sessionM12.ref, null, null, worlds.default, state = Playing)
            val clientM21 = Client(sessionM21.ref, null, null, worlds.default, state = Playing)
            
            val world = worlds.default

            val e1, e2, e3 = Entity(UUID())
            val m11, m12, m21 = Entity(UUID())
             // we have two groups and one solo player
            val r1 = worlds.default.use(e1, new TypedSet() + GroupLeader(members = List(m11, m12)))
            val r2 = worlds.default.use(e2, new TypedSet() + GroupLeader(members = List(m21)))
            val r3 = worlds.default.use(e3, new TypedSet() + GroupLeader())
            val rm11 = worlds.default.use(m11, new TypedSet() + GroupMember(e1))
            val rm12 = worlds.default.use(m12, new TypedSet() + GroupMember(e1))
            val rm21 = worlds.default.use(m21, new TypedSet() + GroupMember(e2))
            
            clientE1.entity = Some(r1) 
            clientE2.entity = Some(r2)
            clientE3.entity = Some(r3)
            clientM11.entity = Some(rm11)
            clientM12.entity = Some(rm12)
            clientM21.entity = Some(rm21)

            clients.add(clientE1)
            clients.add(clientE2)
            clients.add(clientE3)
            clients.add(clientM11)
            clients.add(clientM12)
            clients.add(clientM21)

            // then

            // group 1 leader
            fakePub(chat, sessionE1.ref, ChatMessage(e1, "hi group1 - leader", ChatChannels.Group.toString))
            sessionE1.expectMsg(ChatMessage(e1, "hi group1 - leader", ChatChannels.Group.toString))
            sessionM11.expectMsg(ChatMessage(e1, "hi group1 - leader", ChatChannels.Group.toString))
            sessionM12.expectMsg(ChatMessage(e1, "hi group1 - leader", ChatChannels.Group.toString))
            sessionE2.expectNoMsg
            sessionE3.expectNoMsg
            sessionM21.expectNoMsg

            // group 1 member
            fakePub(chat, sessionM11.ref, ChatMessage(m11, "hi group1 - member", ChatChannels.Group.toString))
            sessionE1.expectMsg(ChatMessage(m11, "hi group1 - member", ChatChannels.Group.toString))
            sessionM11.expectMsg(ChatMessage(m11, "hi group1 - member", ChatChannels.Group.toString))
            sessionM12.expectMsg(ChatMessage(m11, "hi group1 - member", ChatChannels.Group.toString))
            sessionE2.expectNoMsg
            sessionE3.expectNoMsg
            sessionM21.expectNoMsg

            // group 2 leader
            fakePub(chat, sessionE2.ref, ChatMessage(e2, "hi group2 - leader", ChatChannels.Group.toString))
            sessionE2.expectMsg(ChatMessage(e2, "hi group2 - leader", ChatChannels.Group.toString))
            sessionM21.expectMsg(ChatMessage(e2, "hi group2 - leader", ChatChannels.Group.toString))
            sessionE1.expectNoMsg
            sessionE3.expectNoMsg
            sessionM11.expectNoMsg
            sessionM12.expectNoMsg

            // group 2 member
            fakePub(chat, sessionM21.ref, ChatMessage(m21, "hi group2 - member", ChatChannels.Group.toString))
            sessionE2.expectMsg(ChatMessage(m21, "hi group2 - member", ChatChannels.Group.toString))
            sessionM21.expectMsg(ChatMessage(m21, "hi group2 - member", ChatChannels.Group.toString))
            sessionE1.expectNoMsg
            sessionE3.expectNoMsg
            sessionM11.expectNoMsg
            sessionM12.expectNoMsg

            // single
            fakePub(chat, sessionE3.ref, ChatMessage(e3, "hi nobody", ChatChannels.Group.toString))
            // we expect to get some fancy 'you are alone' message
            sessionE3.expectMsgPF() { 
                case ServerMessage(_) => true
            }
            sessionE1.expectNoMsg
            sessionE2.expectNoMsg
            sessionM11.expectNoMsg
            sessionM12.expectNoMsg
            sessionM21.expectNoMsg
        }
    }
}