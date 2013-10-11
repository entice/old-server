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


class PlaySpec extends TestKit(ActorSystem(
    "play-spec", 
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
    val play = TestActorRef[Play]

    // given
    val clients = ClientRegistryExtension(system)
    val worlds = WorldRegistryExtension(system)
    

    override def afterAll  { TestKit.shutdownActorSystem(system) }
    

    "A play controller" must {


        "propagate the world state when receiving a valid play request" in {
            // given our client with some chars
            val session = TestProbe()
            val e1, e2 = Entity(UUID())
            val chars = Map(
                (e1 -> CharacterView(Name("test1"), Appearance())),
                (e2 -> CharacterView(Name("test2"), Appearance()))
            )
            val client = Client(session.ref, null, chars, worlds.default)
            clients.add(client)

            fakePub(play, session.ref, PlayRequest(e1))
            session.expectMsgPF() {
                case PlaySuccess(_, List(EntityView(e1, AllCompsView(_)))) => true
            }
            session.expectNoMsg
        }


        "propagate the world state when receiving a valid change-map request" in {
            // given our client with some chars
            val session = TestProbe()
            val e1, e2 = Entity(UUID())
            val chars = Map(
                (e1 -> CharacterView(Name("test1"), Appearance())),
                (e2 -> CharacterView(Name("test2"), Appearance()))
            )
            val client = Client(session.ref, null, chars, worlds.default, state = Playing)
            val rich = worlds.default.use(e1, new TypedSet[Component]() + Name("test1") + Appearance())
            client.entity = Some(rich)
            clients.add(client)

            worlds.default.name must not be("RandomArenas") // just to make sure this test is actually reasonable

            // must accept a mapchange
            fakePub(play, session.ref, PlayChangeMap("RandomArenas"))
            session.expectMsgPF() {
                case PlaySuccess("RandomArenas", List(EntityView(e1, AllCompsView(_)))) => true
            }
            session.expectNoMsg

            // must have removed the player from the map
            intercept[NoSuchElementException] {
                worlds.default.getRich(e1)
            }
        }


        "bring clients back to the lobby on quitting" in {
            // given our client with some chars
            val session = TestProbe()
            val e1, e2 = Entity(UUID())
            val chars = Map(
                (e1 -> CharacterView(Name("test1"), Appearance())),
                (e2 -> CharacterView(Name("test2"), Appearance()))
            )
            val client = Client(session.ref, null, chars, worlds.default, state = Playing)
            val rich = worlds.default.use(e1, new TypedSet[Component]() + Name("test1") + Appearance())
            client.entity = Some(rich)
            clients.add(client)

            // must accept a quit
            fakePub(play, session.ref, PlayQuit())
            session.expectNoMsg

            // must have removed the player from the map
            intercept[NoSuchElementException] {
                worlds.default.getRich(e1)
            }
        }



        "detect hacks" in {
            val noClient = TestProbe()
            fakePub(play, noClient.ref, PlayRequest(Entity(UUID())))
            noClient.expectMsgClass(classOf[Failure])
            noClient.expectMsg(Kick)

            fakePub(play, noClient.ref, PlayChangeMap("TeamArenas"))
            noClient.expectMsgClass(classOf[Failure])
            noClient.expectMsg(Kick)

            fakePub(play, noClient.ref, PlayQuit())
            noClient.expectMsgClass(classOf[Failure])
            noClient.expectMsg(Kick)

            val noEntity = TestProbe()
            clients.add(Client(noEntity.ref, null, Map(), worlds.default))
            fakePub(play, noEntity.ref, PlayRequest(Entity(UUID())))
            noEntity.expectMsgClass(classOf[Failure])
            noEntity.expectMsg(Kick)

            val wrongState = TestProbe()
            val client = Client(wrongState.ref, null, Map(), worlds.default, state = Playing)
            clients.add(client)
            fakePub(play, wrongState.ref, PlayRequest(Entity(UUID())))
            wrongState.expectMsgClass(classOf[Failure])
            wrongState.expectMsg(Kick)

            client.state = IdleInLobby
            fakePub(play, wrongState.ref, PlayChangeMap("TeamArenas"))
            wrongState.expectMsgClass(classOf[Failure])
            wrongState.expectMsg(Kick)

            fakePub(play, wrongState.ref, PlayQuit())
            wrongState.expectMsgClass(classOf[Failure])
            wrongState.expectMsg(Kick)
        }

    }
}