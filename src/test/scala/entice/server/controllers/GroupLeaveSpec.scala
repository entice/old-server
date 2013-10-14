/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.controllers

import entice.server._, Net._
import entice.server.test._
import entice.server.utils._
import entice.protocol._
import akka.actor._
import akka.testkit._
import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.scalatest.matchers._

import scala.language.postfixOps
import scala.concurrent.duration._


class GroupLeaveSpec extends TestKit(ActorSystem(
    "group-leave-spec", 
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
    val groupLeave = TestActorRef[GroupLeave]

    // given
    val clients = ClientRegistryExtension(system)
    val worlds = WorldRegistryExtension(system)
    

    override def afterAll  { TestKit.shutdownActorSystem(system) }
    

    "A group-leave controller" must {


        "self-kick the client from the group if its a member" in {
            // given
            val session = TestProbe()
            val e1, e2, e3 = Entity(UUID())
            val r1 = worlds.default.use(e1, new TypedSet() + GroupMember(e2))
            val r2 = worlds.default.use(e2, new TypedSet() + GroupLeader(members = List(e1, e3)))
            val r3 = worlds.default.use(e3, new TypedSet() + GroupMember(e2))
            val client = Client(session.ref, null, null, worlds.default, Some(r1), Playing)
            clients.add(client)

            // when
            fakePub(groupLeave, session.ref, GroupKickRequest(e1))
            session.expectNoMsg

            // must
            within(3 seconds) {
                r1.get[GroupMember] must be(None)
                // empty new group for the former leader
                r1.get[GroupLeader] must be(Some(GroupLeader()))
                r2[GroupLeader].members must be(List(e3))
                // no changes in e3
                r3[GroupMember].leader must be(e2)
            }
        }


        "self-kick the client from the group if its a leader" in {
            // given
            val session = TestProbe()
            val e1, e2, e3 = Entity(UUID())
            val r1 = worlds.default.use(e1, new TypedSet() + GroupLeader(members = List(e2, e3)))
            val r2 = worlds.default.use(e2, new TypedSet() + GroupMember(e1))
            val r3 = worlds.default.use(e3, new TypedSet() + GroupMember(e1))
            val client = Client(session.ref, null, null, worlds.default, Some(r1), Playing)
            clients.add(client)

            // when
            fakePub(groupLeave, session.ref, GroupKickRequest(e1))
            session.expectNoMsg

            // must
            within(3 seconds) {
                // empty new group for the former leader
                r1.get[GroupLeader] must be(Some(GroupLeader()))
                // old first member is now the new leader
                r2.get[GroupMember] must be(None)
                r2[GroupLeader].members must be(List(e3))
                // new leader for the former second member
                r3[GroupMember].leader must be(e2)
            }
        }


        "kick group-members if the client is the group-leader" in {
            // given
            val session = TestProbe()
            val e1, e2, e3 = Entity(UUID())
            val r1 = worlds.default.use(e1, new TypedSet() + GroupLeader(members = List(e2, e3)))
            val r2 = worlds.default.use(e2, new TypedSet() + GroupMember(e1))
            val r3 = worlds.default.use(e3, new TypedSet() + GroupMember(e1))
            val client = Client(session.ref, null, null, worlds.default, Some(r1), Playing)
            clients.add(client)

            // when
            fakePub(groupLeave, session.ref, GroupKickRequest(e2))
            session.expectNoMsg

            // must
            within(3 seconds) {
                r1[GroupLeader].members must be(List(e3))
                // former member now has its own group
                r2.get[GroupMember] must be(None)
                r2.get[GroupLeader] must be(Some(GroupLeader()))
                // no changes in e3
                r3[GroupMember].leader must be(e1)
            }
        }
    }
}