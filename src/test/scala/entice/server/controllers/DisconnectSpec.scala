/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.controllers

import entice.server._, Net._
import entice.server.test._
import entice.server.utils._
import entice.server.world._
import entice.server.world.systems._
import entice.protocol._
import akka.actor._
import akka.testkit._
import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.scalatest.matchers._

import scala.language.postfixOps
import scala.concurrent.duration._


class DisconnectSpec extends TestKit(ActorSystem(
    "disconnect-spec", 
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
    val disc = TestActorRef[Disconnect]

    // given
    val clients = ClientRegistryExtension(system)
    val worlds  = WorldRegistryExtension(system)


    override def afterAll  { TestKit.shutdownActorSystem(system) }


    "The disconnect controller" must {


        "check if sessions have been terminated and remove leftover data" in {
            //given
            val session = TestProbe()
            val client = Client(session.ref, null, null, worlds.default)
            val entity = client.world.create(new TypedSet[Component]() + Name("world-diff-spec1"))
            client.entity = Some(entity)
            clients.add(client)

            clients.get(session.ref) must be(Some(client))
            clients.get(entity) must be(Some(client))
            clients.getAll must be(List(client))
            clients.getAll find {_.session == session.ref} must not be(None)

            // must
            fakePub(disc, self, LostSession(session.ref))

            within(3 seconds) {
                clients.get(session.ref) must be(None)
                clients.get(entity) must be(None)
                clients.getAll must be(Nil)
                clients.getAll find {_.session == session.ref} must be(None)
                client.world.getRich(entity.entity) must be (None)
            }
        }
    }
}