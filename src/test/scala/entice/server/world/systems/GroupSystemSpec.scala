/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.world.systems

import entice.server._, Net._
import entice.server.test._
import entice.server.utils._
import entice.server.world._
import entice.protocol._
import akka.actor._
import akka.testkit._
import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.scalatest.matchers._

import scala.language.postfixOps
import scala.concurrent.duration._


class GroupSystemSpec extends TestKit(ActorSystem(
    "group-sys-spec", 
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
    val groupSys = TestActorRef[GroupSystem]

    // given
    val worlds = WorldRegistryExtension(system)
    

    override def afterAll  { TestKit.shutdownActorSystem(system) }
    

    "A group-system" must {

        "understand invites" in {
            // given
            val r1 = worlds.default.create(new TypedSet() + GroupLeader())
            val r2 = worlds.default.create(new TypedSet() + GroupLeader())

            // when
            fakePub(groupSys, self, GroupInvite(r1, r2))
            expectNoMsg

            // must
            within(3 seconds) {
                r1[GroupLeader].invited must be(List(r2.entity))
                r2[GroupLeader].joinRequests must be(List(r1.entity))
            }
        }


        "understand declines" in {
            // given
            val e1, e2, e3 = Entity(UUID())
            val r1 = worlds.default.use(e1, new TypedSet() + GroupLeader(invited = List(e2), joinRequests = List(e3)))
            val r2 = worlds.default.use(e2, new TypedSet() + GroupLeader(joinRequests = List(e1)))
            val r3 = worlds.default.use(e3, new TypedSet() + GroupLeader(invited = List(e1)))

            // when
            fakePub(groupSys, self, GroupDecline(r1, r2))
            expectNoMsg

            // must
            within(3 seconds) {
                r1[GroupLeader].invited must be(Nil)
                r1[GroupLeader].joinRequests must be(List(e3))
                r2[GroupLeader].joinRequests must be(Nil)
            }

            // when
            fakePub(groupSys, self, GroupDecline(r1, r3))
            expectNoMsg

            // must
            within(3 seconds) {
                r1[GroupLeader].joinRequests must be(Nil)
                r3[GroupLeader].invited must be(Nil)
            }
        }


        "understand join-request accepts" in {
            // given
            val e1, e2 = Entity(UUID())
            val m11, m12, m21, m22 = Entity(UUID())
             // e2 has invited us (e1) at some point... (but we already have two groups)
            val r1 = worlds.default.use(e1, new TypedSet() + GroupLeader(members = List(m11, m12), joinRequests = List(e2)))
            val r2 = worlds.default.use(e2, new TypedSet() + GroupLeader(members = List(m21, m22), invited = List(e1)))
            val rm11 = worlds.default.use(m11, new TypedSet() + GroupMember(e1))
            val rm12 = worlds.default.use(m12, new TypedSet() + GroupMember(e1))
            val rm21 = worlds.default.use(m21, new TypedSet() + GroupMember(e2))
            val rm22 = worlds.default.use(m22, new TypedSet() + GroupMember(e2))

            // when
            fakePub(groupSys, self, GroupAccept(r1, r2))
            expectNoMsg

            // must
            within(3 seconds) {
                // me is now a member
                r1.get[GroupLeader] must be(None)
                r1[GroupMember].leader must be(e2)
                // the other leader now has me and my members as members
                r2[GroupLeader].members must be(List(m21, m22, e1, m11, m12))
                r2[GroupLeader].invited must be(Nil)
                // new leader for my former members
                rm11[GroupMember].leader must be(e2)
                rm12[GroupMember].leader must be(e2)
            }
        }


        "understand when a member leaves the group (or despawns)" in {
            // given
            val e1, e2, e3 = Entity(UUID())
            val r1 = worlds.default.use(e1, new TypedSet() + GroupLeader(members = List(e2, e3)))
            val r2 = worlds.default.use(e2, new TypedSet() + GroupMember(e1))
            val r3 = worlds.default.use(e3, new TypedSet() + GroupMember(e1))

            // when
            fakePub(groupSys, self, GroupLeave(r2))
            expectNoMsg

            // must
            within(3 seconds) {
                r1[GroupLeader].members must be(List(e3))
                // empty new group for the former member
                r2.get[GroupMember] must be(None)
                r2.get[GroupLeader] must be(Some(GroupLeader()))
                // no changes for the former second member
                r3[GroupMember].leader must be(e1)
            }

            // when
            fakePub(groupSys, self, Despawned(worlds.default, e3, new TypedSet() + GroupMember(e1)))
            expectNoMsg

            // must
            within(3 seconds) {
                r1[GroupLeader].members must be(Nil)
            }
        }


        "understand when the leader leaves the group (or despawns)" in {
            // given
            val e1, e2, e3 = Entity(UUID())
            val r1 = worlds.default.use(e1, new TypedSet() + GroupLeader(members = List(e2, e3)))
            val r2 = worlds.default.use(e2, new TypedSet() + GroupMember(e1))
            val r3 = worlds.default.use(e3, new TypedSet() + GroupMember(e1))

            // when
            fakePub(groupSys, self, GroupLeave(r1))
            expectNoMsg

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

            // when
            fakePub(groupSys, self, Despawned(worlds.default, e2, new TypedSet() + GroupLeader(members = List(e3))))
            expectNoMsg

            // must
            within(3 seconds) {
                r3.get[GroupMember] must be(None)
                r3.get[GroupLeader] must be(Some(GroupLeader()))
            }
        }


        "understand member kicks" in {
            // given
            val e1, e2, e3 = Entity(UUID())
            val r1 = worlds.default.use(e1, new TypedSet() + GroupLeader(members = List(e2, e3)))
            val r2 = worlds.default.use(e2, new TypedSet() + GroupMember(e1))
            val r3 = worlds.default.use(e3, new TypedSet() + GroupMember(e1))

            // when
            fakePub(groupSys, self, GroupKick(r1, r2))
            expectNoMsg

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