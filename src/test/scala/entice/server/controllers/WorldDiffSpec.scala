/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.controllers

import entice.server._, Net._
import entice.server.test._
import entice.server.utils._
import entice.server.world._
import entice.protocol._
import akka.actor._
import akka.testkit._
import com.typesafe.config.ConfigFactory
import com.mongodb.casbah.commons.Imports._
import org.scalatest._
import org.scalatest.matchers._


class WorldDiffSpec extends TestKit(ActorSystem(
    "world-diff-spec", 
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
    val stopWatch = TestStopWatch()
    val worldDiff = TestActorRef(new WorldDiff(stopWatch))

    // given
    val clients = ClientRegistryExtension(system)
    val worlds = WorldRegistryExtension(system)

    override def beforeAll { stopWatch.set(Config.get.minTick + 1) }

    override def afterAll  { TestKit.shutdownActorSystem(system) }


    "A world-diff controller" must {


        "capture components that changed, and send them to playing clients" in {

            // given
            val session = TestProbe()
            val client = Client(session.ref, null, Map(), worlds.default, None, state = Playing)
            val entity = client.world.create(new TypedSet[Component]() + Name("world-diff-spec1"))
            client.entity = Some(entity)
            clients.add(client)

            // when changing and then ticking
            entity.set(Name("world-diff-spec2"))
            fakePub(worldDiff, self, Tick())

            session.expectMsgPF() {
                case UpdateCommand(t, l1, l2, _)
                    if (l1.contains(EntityView(entity.entity, Nil, List(Name("world-diff-spec2")), Nil))
                    &&  l2.contains(entity.entity) 
                    &&  t == Config.get.minTick + 1) => true
            }
            session.expectNoMsg

            // when changing and then flushing
            entity.set(Name("world-diff-spec3"))
            fakePub(worldDiff, self, Flush())

            session.expectMsgPF() {
                case UpdateCommand(t, l1, _, _)
                     if (l1.contains(EntityView(entity.entity, List(Name("world-diff-spec3")), Nil, Nil))
                     &&  t == Config.get.minTick + 1) => true
            }
        }
    }
}