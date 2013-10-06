/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.controllers

import entice.server._, Net._
import entice.server.utils._
import entice.protocol._
import akka.actor._
import akka.testkit._
import org.scalatest._
import org.scalatest.matchers._


class PlaySpec(_system: ActorSystem) extends TestKit(_system)
    with WordSpec
    with MustMatchers 
    with BeforeAndAfterAll
    with ImplicitSender {

    def this() = this(ActorSystem("play-spec"))

    def testPub(probe: ActorRef, msg: Message) { 
        MessageBusExtension(_system).publish(MessageEvent(probe, msg)) 
    }

    override def beforeAll {
        val play = _system.actorOf(Props[Play])
    }

    override def afterAll {
        TestKit.shutdownActorSystem(_system)
    }


    "A play controller" must {

        val clients = ClientRegistryExtension(_system)
        

        "propagate the world state when receiving a valid play request" in {
            // given our client with some chars
            val session = TestProbe()
            val e1, e2 = Entity(UUID())
            val chars = Map(
                (e1 -> CharacterView(Name("test1"), Appearance())),
                (e2 -> CharacterView(Name("test2"), Appearance()))
            )
            val client = Client(session.ref, null, chars)
            clients.add(client)

            testPub(session.ref, PlayRequest(e1))
            session.expectMsgPF() {
                case PlaySuccess(List(EntityView(e1, AllCompsView(_)))) => true
            }
            session.expectNoMsg
        }


        "detect hacks" in {
            val noClient = TestProbe()
            testPub(noClient.ref, PlayRequest(Entity(UUID())))
            noClient.expectMsgPF() {
                case PlayFail(errorMsg) if errorMsg != "" => true
            }
            noClient.expectMsg(Kick)

            val noEntity = TestProbe()
            clients.add(Client(noEntity.ref, null, Map()))
            testPub(noEntity.ref, PlayRequest(Entity(UUID())))
            noEntity.expectMsgPF() {
                case PlayFail(errorMsg) if errorMsg != "" => true
            }
            noEntity.expectMsg(Kick)
        }

    }
}