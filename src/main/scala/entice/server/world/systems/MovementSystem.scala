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


class MovementSystem(
    stopWatch : StopWatch) extends System[Position :: Movement :: HNil] with Actor with Subscriber with Worlds with ActorLogging {

    val subscriptions = classOf[Tick] :: Nil
    override def preStart { register }


    def receive = {
        case MessageEvent(_, Tick()) => worlds.getAll foreach update
    }


    override def update(world: World) {
        val timeDiff = stopWatch.current

        entities(world)
            .filter  { e => 
                val curPos  = e[Position].pos
                val curGoal = e[Movement].goal
                (e[Movement].moveState != NotMoving &&
                 curGoal - curPos      != Coord2D(0, 0)) 
            }
            .foreach { e =>
                val curPos  = e[Position].pos
                val curGoal = e[Movement].goal
                val curDir  = curGoal - curPos

                // TODO: add movementspeed here, add trapezoid
                // the 0.288 is actually distance per milliseconds, so
                // it would be 288 terrain tiles per second
                val nextPos = curPos + ((curDir.unit * 0.288F) * timeDiff)

                // check out the next position and set it
                // world.pmap.nextValidPosition(curPos, nextPos) match {
                //     case Some(pos) if (pos != curPos) => 
                //         // set the new position
                //         e.set[Position](e[Position].copy(pos = pos))
                //         // get a new goal
                //         world.pmap.farthestPosition(pos, curDir) match {
                //             case Some(goal) if (goal != pos) =>
                //                 e.set[Movement](e[Movement].copy(goal = goal))
                //             case _ =>
                //                 e.set[Movement](e[Movement].copy(goal = pos, state = NotMoving.toString))
                //         }
                //     case _ => 
                //         e.set[Movement](e[Movement].copy(goal = curPos, state = NotMoving.toString))
                // }

                world.pmap.farthestPosition(curPos, curDir) match {
                    // we are already there
                    case Some(goal) if (curPos == goal) =>
                        e.set[Movement](e[Movement].copy(goal = curPos, state = NotMoving.toString))
                    // we can walk there, (or almost there) then stop
                    case Some(goal) if ((nextPos == goal) || ((goal - curPos).len < (nextPos - curPos).len)) =>
                        e.set[Position](e[Position].copy(pos = goal))
                        e.set[Movement](e[Movement].copy(goal = goal, state = NotMoving.toString))
                    // we can walk on further
                    case Some(goal) =>
                        e.set[Position](e[Position].copy(pos = nextPos))
                        e.set[Movement](e[Movement].copy(goal = goal))
                    // our position and or direction is invalid
                    case _ =>
                        log.info("Entity at invalid position. [TimeDelta: " + timeDiff + " | Position: " + curPos + " | Direction: " + curDir + " | Next Position: " + nextPos + "]")
                        e.set[Movement](e[Movement].copy(goal = curPos, state = NotMoving.toString))
                }
                
            }

        // NOTE: yes, it IS curcial to reset the timer at this point :P
        stopWatch.reset
    }
}
