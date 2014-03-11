/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.world.systems

import entice.server._, Net._
import entice.server.test._
import entice.server.utils._
import entice.server.world._
import entice.server.physics._
import entice.protocol._, MoveState._
import akka.actor._
import akka.testkit._
import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.scalatest.matchers._

import scala.language.postfixOps
import scala.concurrent.duration._


class MovementSystemSpec extends TestKit(ActorSystem(
    "movement-sys-spec", 
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

    // since this is a time-critical test,
    // we need to replace the stopwatch of the actor
    // with our implementation

    // actor under test
    val stopWatch = TestStopWatch()
    val moveSys = TestActorRef(new MovementSystem(stopWatch))

    // given
    val msgBus = MessageBusExtension(system)
    val pmap   = PathingMap.fromString(MovementSystemSpec.sampleMap); pmap must not be(None)
    val world  = new World("SampleMap", msgBus, pmap.get)
    WorldRegistryExtension(system).add(world) // dont forget to add the world to the registry
    

    override def afterAll  { TestKit.shutdownActorSystem(system) }
    

    "A movement-system" must {

        "not move entities that do not move" in {
            // given
            val r1 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 0)) + Movement(Coord2D(0, 0), NotMoving.toString))

            // when
            fakePub(moveSys, self, Tick())
            expectNoMsg

            // must
            within(3 seconds) {
                r1[Position].pos must be(Coord2D(0, 0))
                r1[Movement].goal must be(Coord2D(0, 0))
                r1[Movement].moveState must be(NotMoving)
            }
        }


        "move entities in +x (no collision)" in {
            // given (one normal, two edge cases, one invalid case)                                    v- remember, this is the GOAL! 
            val r1 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 0))       + Movement(Coord2D(500, 0), Moving.toString))
            val r2 = world.create(new TypedSet[Component]() + Position(Coord2D(-1000, 0))   + Movement(Coord2D(-500, 0), Moving.toString))
            val r3 = world.create(new TypedSet[Component]() + Position(Coord2D(1000, 0))    + Movement(Coord2D(1500, 0), Moving.toString))
            val r4 = world.create(new TypedSet[Component]() + Position(Coord2D(1000.1F, 0)) + Movement(Coord2D(1500.1F, 0), Moving.toString))

            // when
            stopWatch.set(1000)
            fakePub(moveSys, self, Tick())
            expectNoMsg

            // must
            within(1 seconds) {
                // normal
                r1[Position].pos must be(Coord2D(0 + 288, 0))
                r1[Movement].goal must be(Coord2D(1000, 0))
                r1[Movement].moveState must be(Moving)

                // edge case 1
                r2[Position].pos must be(Coord2D(-1000 + 288, 0))
                r2[Movement].goal must be(Coord2D(1000, 0))
                r2[Movement].moveState must be(Moving)

                // edge case 2
                r3[Position].pos must be(Coord2D(1000, 0))
                r3[Movement].goal must be(Coord2D(1000, 0))
                r3[Movement].moveState must be(NotMoving)

                // invalid
                r4[Position].pos must be(Coord2D(1000.1F, 0))
                r4[Movement].goal must be(Coord2D(1000.1F, 0))
                r4[Movement].moveState must be(NotMoving)
            }
        }


        "move entities in -x (no collision)" in {
            // given (one normal, two edge cases, one invalid case)                                    v- remember, this is the GOAL! 
            val r1 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 0))       + Movement(Coord2D(-500, 0), Moving.toString))
            val r2 = world.create(new TypedSet[Component]() + Position(Coord2D(-1000, 0))   + Movement(Coord2D(-1500, 0), Moving.toString))
            val r3 = world.create(new TypedSet[Component]() + Position(Coord2D(1000, 0))    + Movement(Coord2D(500, 0), Moving.toString))
            val r4 = world.create(new TypedSet[Component]() + Position(Coord2D(1000.1F, 0)) + Movement(Coord2D(500.1F, 0), Moving.toString))

            // when
            stopWatch.set(1000)
            fakePub(moveSys, self, Tick())
            expectNoMsg

            // must
            within(1 seconds) {
                // normal
                r1[Position].pos must be(Coord2D(0 - 288, 0))
                r1[Movement].goal must be(Coord2D(-1000, 0))
                r1[Movement].moveState must be(Moving)

                // edge case 1
                r2[Position].pos must be(Coord2D(-1000, 0))
                r2[Movement].goal must be(Coord2D(-1000, 0))
                r2[Movement].moveState must be(NotMoving)

                // edge case 2
                r3[Position].pos must be(Coord2D(1000 - 288, 0))
                r3[Movement].goal must be(Coord2D(-1000, 0))
                r3[Movement].moveState must be(Moving)

                // invalid
                r4[Position].pos must be(Coord2D(1000.1F, 0))
                r4[Movement].goal must be(Coord2D(1000.1F, 0))
                r4[Movement].moveState must be(NotMoving)
            }
        }


        "move entities in +y (no collision)" in {
            // given (one normal, two edge cases, one invalid case)                                    v- remember, this is the GOAL! 
            val r1 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 0))       + Movement(Coord2D(0, 500), Moving.toString))
            val r2 = world.create(new TypedSet[Component]() + Position(Coord2D(0, -2000))   + Movement(Coord2D(0, -1500), Moving.toString))
            val r3 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 1000))    + Movement(Coord2D(0, 1500), Moving.toString))
            val r4 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 1000.1F)) + Movement(Coord2D(0, 1500.1F), Moving.toString))

            // when
            stopWatch.set(1000)
            fakePub(moveSys, self, Tick())
            expectNoMsg

            // must
            within(1 seconds) {
                // normal
                r1[Position].pos must be(Coord2D(0, 0 + 288))
                r1[Movement].goal must be(Coord2D(0, 1000))
                r1[Movement].moveState must be(Moving)

                // edge case 1
                r2[Position].pos must be(Coord2D(0, -2000 + 288))
                r2[Movement].goal must be(Coord2D(0, 1000))
                r2[Movement].moveState must be(Moving)

                // edge case 2
                r3[Position].pos must be(Coord2D(0, 1000))
                r3[Movement].goal must be(Coord2D(0, 1000))
                r3[Movement].moveState must be(NotMoving)

                // invalid
                r4[Position].pos must be(Coord2D(0, 1000.1F))
                r4[Movement].goal must be(Coord2D(0, 1000.1F))
                r4[Movement].moveState must be(NotMoving)
            }
        }


        "move entities in -y (no collision)" in {
            // given (one normal, two edge cases, one invalid case)                                    v- remember, this is the GOAL! 
            val r1 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 0))       + Movement(Coord2D(0, -500), Moving.toString))
            val r2 = world.create(new TypedSet[Component]() + Position(Coord2D(0, -2000))   + Movement(Coord2D(0, -2500), Moving.toString))
            val r3 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 1000))    + Movement(Coord2D(0, 500), Moving.toString))
            val r4 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 1000.1F)) + Movement(Coord2D(0, 500.1F), Moving.toString))

            // when
            stopWatch.set(1000)
            fakePub(moveSys, self, Tick())
            expectNoMsg

            // must
            within(1 seconds) {
                // normal
                r1[Position].pos must be(Coord2D(0, 0 - 288))
                r1[Movement].goal must be(Coord2D(0, -2000))
                r1[Movement].moveState must be(Moving)

                // edge case 1
                r2[Position].pos must be(Coord2D(0, -2000))
                r2[Movement].goal must be(Coord2D(0, -2000))
                r2[Movement].moveState must be(NotMoving)

                // edge case 2
                r3[Position].pos must be(Coord2D(0, 1000 - 288))
                r3[Movement].goal must be(Coord2D(0, -2000))
                r3[Movement].moveState must be(Moving)

                // invalid
                r4[Position].pos must be(Coord2D(0, 1000.1F))
                r4[Movement].goal must be(Coord2D(0, 1000.1F))
                r4[Movement].moveState must be(NotMoving)
            }
        }

        "move entities in +x (with collision)" in {
            // given (one normal, two edge cases, one invalid case)                                    v- remember, this is the GOAL! 
            val r1 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 0))       + Movement(Coord2D(500, 0), Moving.toString))
            val r2 = world.create(new TypedSet[Component]() + Position(Coord2D(-1000, 0))   + Movement(Coord2D(-500, 0), Moving.toString))
            val r3 = world.create(new TypedSet[Component]() + Position(Coord2D(1000, 0))    + Movement(Coord2D(1500, 0), Moving.toString))
            val r4 = world.create(new TypedSet[Component]() + Position(Coord2D(1000.1F, 0)) + Movement(Coord2D(1500.1F, 0), Moving.toString))

            // when
            stopWatch.set(10000)
            fakePub(moveSys, self, Tick())
            expectNoMsg

            // must
            within(1 seconds) {
                // normal
                r1[Position].pos must be(Coord2D(1000, 0))
                r1[Movement].goal must be(Coord2D(1000, 0))
                r1[Movement].moveState must be(NotMoving)

                // edge case 1
                r2[Position].pos must be(Coord2D(1000, 0))
                r2[Movement].goal must be(Coord2D(1000, 0))
                r2[Movement].moveState must be(NotMoving)

                // edge case 2
                r3[Position].pos must be(Coord2D(1000, 0))
                r3[Movement].goal must be(Coord2D(1000, 0))
                r3[Movement].moveState must be(NotMoving)

                // invalid
                r4[Position].pos must be(Coord2D(1000.1F, 0))
                r4[Movement].goal must be(Coord2D(1000.1F, 0))
                r4[Movement].moveState must be(NotMoving)
            }
        }


        "move entities in -x (with collision)" in {
            // given (one normal, two edge cases, one invalid case)                                    v- remember, this is the GOAL! 
            val r1 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 0))       + Movement(Coord2D(-500, 0), Moving.toString))
            val r2 = world.create(new TypedSet[Component]() + Position(Coord2D(-1000, 0))   + Movement(Coord2D(-1500, 0), Moving.toString))
            val r3 = world.create(new TypedSet[Component]() + Position(Coord2D(1000, 0))    + Movement(Coord2D(500, 0), Moving.toString))
            val r4 = world.create(new TypedSet[Component]() + Position(Coord2D(1000.1F, 0)) + Movement(Coord2D(500.1F, 0), Moving.toString))

            // when
            stopWatch.set(10000)
            fakePub(moveSys, self, Tick())
            expectNoMsg

            // must
            within(1 seconds) {
                // normal
                r1[Position].pos must be(Coord2D(-1000, 0))
                r1[Movement].goal must be(Coord2D(-1000, 0))
                r1[Movement].moveState must be(NotMoving)

                // edge case 1
                r2[Position].pos must be(Coord2D(-1000, 0))
                r2[Movement].goal must be(Coord2D(-1000, 0))
                r2[Movement].moveState must be(NotMoving)

                // edge case 2
                r3[Position].pos must be(Coord2D(-1000, 0))
                r3[Movement].goal must be(Coord2D(-1000, 0))
                r3[Movement].moveState must be(NotMoving)

                // invalid
                r4[Position].pos must be(Coord2D(1000.1F, 0))
                r4[Movement].goal must be(Coord2D(1000.1F, 0))
                r4[Movement].moveState must be(NotMoving)
            }
        }


        "move entities in +y (with collision)" in {
            // given (one normal, two edge cases, one invalid case)                                    v- remember, this is the GOAL! 
            val r1 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 0))       + Movement(Coord2D(0, 500), Moving.toString))
            val r2 = world.create(new TypedSet[Component]() + Position(Coord2D(0, -2000))   + Movement(Coord2D(0, -1500), Moving.toString))
            val r3 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 1000))    + Movement(Coord2D(0, 1500), Moving.toString))
            val r4 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 1000.1F)) + Movement(Coord2D(0, 1500.1F), Moving.toString))

            // when
            stopWatch.set(15000) // cannot be 10000 or otherwise r2 wouldnt collide
            fakePub(moveSys, self, Tick())
            expectNoMsg

            // must
            within(1 seconds) {
                // normal
                r1[Position].pos must be(Coord2D(0, 1000))
                r1[Movement].goal must be(Coord2D(0, 1000))
                r1[Movement].moveState must be(NotMoving)

                // edge case 1
                r2[Position].pos must be(Coord2D(0, 1000))
                r2[Movement].goal must be(Coord2D(0, 1000))
                r2[Movement].moveState must be(NotMoving)

                // edge case 2
                r3[Position].pos must be(Coord2D(0, 1000))
                r3[Movement].goal must be(Coord2D(0, 1000))
                r3[Movement].moveState must be(NotMoving)

                // invalid
                r4[Position].pos must be(Coord2D(0, 1000.1F))
                r4[Movement].goal must be(Coord2D(0, 1000.1F))
                r4[Movement].moveState must be(NotMoving)
            }
        }


        "move entities in -y (with collision)" in {
            // given (one normal, two edge cases, one invalid case)                                    v- remember, this is the GOAL! 
            val r1 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 0))       + Movement(Coord2D(0, -500), Moving.toString))
            val r2 = world.create(new TypedSet[Component]() + Position(Coord2D(0, -2000))   + Movement(Coord2D(0, -2500), Moving.toString))
            val r3 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 1000))    + Movement(Coord2D(0, 500), Moving.toString))
            val r4 = world.create(new TypedSet[Component]() + Position(Coord2D(0, 1000.1F)) + Movement(Coord2D(0, 500.1F), Moving.toString))

            // when
            stopWatch.set(15000) // cannot be 10000 or otherwise r3 wouldnt collide
            fakePub(moveSys, self, Tick())
            expectNoMsg

            // must
            within(1 seconds) {
                // normal
                r1[Position].pos must be(Coord2D(0, -2000))
                r1[Movement].goal must be(Coord2D(0, -2000))
                r1[Movement].moveState must be(NotMoving)

                // edge case 1
                r2[Position].pos must be(Coord2D(0, -2000))
                r2[Movement].goal must be(Coord2D(0, -2000))
                r2[Movement].moveState must be(NotMoving)

                // edge case 2
                r3[Position].pos must be(Coord2D(0, -2000))
                r3[Movement].goal must be(Coord2D(0, -2000))
                r3[Movement].moveState must be(NotMoving)

                // invalid
                r4[Position].pos must be(Coord2D(0, 1000.1F))
                r4[Movement].goal must be(Coord2D(0, 1000.1F))
                r4[Movement].moveState must be(NotMoving)
            }
        }
    }
}

/**
 * Layout of the sample pathing map:
 *
 * 1st rectangle:
 * spanning from -1000, +1000 -> +1000, -1000
 * 
 * 2nd rectangle: (directly south of the first)
 * spanning from -1000, -1000 -> +1000, -2000
 *
 */
object MovementSystemSpec {
    val sampleMap = """
{
    "trapezoids":
    [
        {
            "id":0,
            "north":
            {
                "west":-1000,
                "east":1000,
                "y":1000
            },
            "south":
            {
                "west":-1000,
                "east":1000,
                "y":-1000
            },
            "west":
            {
                "north":{"x":-1000,"y":1000},
                "south":{"x":-1000,"y":-1000}
            },
            "east":
            {
                "north":{"x":1000,"y":1000},
                "south":{"x":1000,"y":-1000}
            }
        },
        {
            "id":1,
            "north":
            {
                "west":-1000,
                "east":1000,
                "y":-1000
            },
            "south":
            {
                "west":-1000,
                "east":1000,
                "y":-2000
            },
            "west":
            {
                "north":{"x":-1000,"y":-1000},
                "south":{"x":-1000,"y":-2000}
            },
            "east":
            {
                "north":{"x":1000,"y":-1000},
                "south":{"x":1000,"y":-2000}
            }
        }
    ],
    "connections":
    [
        {
            "id":0,
            "west":-1000,
            "east":1000,
            "y":-1000,
            "north":0,
            "south":1
        }
    ]
}"""
}