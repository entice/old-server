/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world.systems

import entice.server._
import entice.server.utils._
import entice.server.world._
import entice.server.pathfinding._, Geometry._
import entice.protocol._, MoveState._
import akka.actor._
import shapeless._


class MovementSystem extends System[Position :: Movement :: HNil] with Actor with Subscriber with Worlds{

    val subscriptions = classOf[Tick] :: Nil
    override def preStart { register }

    val stopWatch = StopWatch()

    def receive = {
        case Tick => worlds.getAll foreach update
    }


    override def update(world: World) {
        val timeDiff = stopWatch.current

        entities(world)
            .filter  { e => 
                (e[Movement].moveState != NotMoving &&
                 e[Movement].dir       != Coord2D(0, 0)) 
            }
            .foreach { e =>
                val curPos = e[Position].pos
                val curDir = e[Movement].dir

                // TODO: add movementspeed here, add trapezoid
                val nextPos = (curPos + (curDir.unit * 288)) * timeDiff

                // check out the next position and set it
                world.pmap.nextValidPosition(curPos, nextPos) match {
                    case Some(pos) if (pos != curPos) => 
                        e.set[Position](e[Position].copy(pos = pos))
                    case _ => 
                        e.set[Movement](e[Movement].copy(state = NotMoving.toString))
                }
                
            }
    }
}