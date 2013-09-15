/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.protocol._
import entice.protocol.utils._
import entice.protocol.utils.MessageBus._

import entice.server.game._

import akka.actor._
import akka.testkit._

import org.scalatest._
import org.scalatest.matchers._

import scala.concurrent.duration._


 
class GameServerSpec(_system: ActorSystem) extends TestKit(_system)

    // given components
    with CoreSlice
    with GameApiSlice

    with WordSpec
    with MustMatchers 
    with BeforeAndAfterAll
    with ImplicitSender {


    import SessionActor._
    import ReactorActor._

    override lazy val actorSystem = _system

 
    def this() = this(ActorSystem("game-server-spec"))


    def testPub(id: UUID, probe: ActorRef, msg: Message) { 
        messageBus.publish(MessageEvent(Sender(id, probe), msg)) 
    }
 

    override def afterAll {
        TestKit.shutdownActorSystem(_system)
    }
 

    "A game server" must {

        // given a login server testprobe
        val lsProbe = TestProbe()
   

        "accept new players when requested by the LS" in {
            val playerId = UUID() 
            testPub(UUID(), lsProbe.ref, AddPlayer(playerId, 313373))
            lsProbe.expectMsg(WaitingForPlayer(playerId))
            lsProbe.expectNoMsg
        }


        "let new players connect, if requested" in {
            val playerId = UUID() 
            testPub(UUID(), lsProbe.ref, AddPlayer(playerId, 41735))
            lsProbe.expectMsg(WaitingForPlayer(playerId))
            lsProbe.expectNoMsg

            val probe = TestProbe()
            val id = UUID()
            testPub(id, probe.ref, PlayRequest(41735))
            // register the session with the server and then send a playsuccess
            probe.expectMsgClass(classOf[NewUUID])
            probe.expectMsgClass(classOf[Reactor])
            probe.expectMsgPF() {
                case PlaySuccess(e1, l1) 
                    if (   l1.filter(_.entity == e1).length == 1
                        && e1.uuid == playerId) => true
            }
            probe.expectNoMsg
        }


        "capture components that changed, and send them to playing clients" in {
            // given
            val probe = TestProbe()
            val id = UUID()
            // create register and update the player of this testsession
            val player = Player(id, probe.ref, entityManager)
            playerRegistry += player
            player.state = Playing
            val comps = entityManager.getCompsOf(player.entity).get
            val name = entityManager.getCompBy(player.entity, classOf[Name]).get

            // when
            name.name = "blubb"
            testPub(id, probe.ref, Tick())
            probe.expectMsgPF() {
                case GameUpdate(_, l1, l2, _)
                     if (  l1.contains(EntityView(player.entity, comps))
                        && l2.contains(player.entity)) => true
            }
            probe.expectNoMsg

            name.name = "hello"
            testPub(id, probe.ref, Tick())
            probe.expectMsgPF() {
                case GameUpdate(_, l1, _, _)
                     if (l1.contains(EntityView(player.entity, Set(Name("hello"))))) => true
            }
        }


        "change an entities position and movement according to a movement request" in {
            // given
            val probe = TestProbe()
            val id = UUID()
            // create register and update the player of this testsession
            val player = Player(id, probe.ref, entityManager)
            playerRegistry += player
            player.state = Playing
            val pos = entityManager.getCompBy(player.entity, classOf[Position]).get
            val move = entityManager.getCompBy(player.entity, classOf[Movement]).get
            // when
            testPub(id, probe.ref, MoveRequest(Position(Coord2D(2, 2)), Movement(dir = Coord2D(3, 3), state = "Moving")))
            within(200 millis) {
                pos.pos must be(Coord2D(2, 2))
                move.dir must be(Coord2D(3, 3))
                move.state must be("Moving")
            }
        }
    }
}