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


class CharDeleteSpec extends TestKit(ActorSystem(
    "char-delete-spec", 
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
    val charDelete = TestActorRef[CharDelete]

    // given
    val clients = ClientRegistryExtension(system)
    val worlds  = WorldRegistryExtension(system)

    
    override def afterAll  { TestKit.shutdownActorSystem(system) }


    "A char-delete controller" must {


        "delete chars when receiving a valid request" in {
            //given
            val session = TestProbe()
            val entity = Entity(UUID())
            val char = Character(accountId = new ObjectId(), name = Name("char-delete-spec-test"))
            val client = Client(
                session.ref, 
                null, 
                Map((entity -> CharacterView(char.name, Appearance()))), 
                worlds.default)

            Character.create(char)
            clients.add(client)

            // must
            fakePub(charDelete, session.ref, CharDelete(entity))
            session.expectNoMsg

            Character.read(char) must be(None)
        }


        "detect hacks" in {
            val noClient = TestProbe()
            fakePub(charDelete, noClient.ref, CharDelete(Entity(UUID())))
            noClient.expectMsgClass(classOf[Failure])
            noClient.expectMsg(Kick)

            val noEntity = TestProbe()
            clients.add(Client(noEntity.ref, null, Map(), worlds.default))
            fakePub(charDelete, noEntity.ref, CharDelete(Entity(UUID())))
            noEntity.expectMsgClass(classOf[Failure])
            noEntity.expectMsg(Kick)
        }
    }
}