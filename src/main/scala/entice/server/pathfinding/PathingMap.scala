/**
 * For copyright information see the LICENSE document.
 */

package entice.server.pathfinding

import entice.protocol.Coord2D
import play.api.libs.json._
import info.akshaal.json.jsonmacro._
import scala.io._
import scala.util._


/**
 * Simplified non-associative version, for serialization purposes.
 */
case class SimplePathingMap(
    trapezoids: Array[SimpleTrapezoid],
    connections: Array[SimpleHorizontalConnection])


/**
 * Consists of the walkable trapezoids and their connections.
 */
class PathingMap(
    val trapezoids: List[Trapezoid],
    val connections: Set[HorizontalConnection]) {


    /**
     * Checks which trapezoid the position is in
     */
    def trapezoidFor(pos: Coord2D): Option[Trapezoid] = {
        trapezoids filter { _.contains(pos) } match {
            case head :: tail => Some(head)
            case Nil          => None
        }
    }


    /**
     * Checks if there is a linear path from a position in a direction
     * to a certain trapezoid
     */
    def hasDirectPath(pos: Coord2D, dir: Coord2D, goal: Trapezoid, current: Option[Trapezoid] = None): Boolean = {
        val currentTrap = if (current.isDefined) current else trapezoidFor(pos)
        if (!currentTrap.isDefined) return false
        if (goal == currentTrap.get) return true
        currentTrap.get.crossedConnection(pos, dir) match {
            case Some((con, loc)) =>
                // go to the border of the other trapezoid, then start the search anew
                hasDirectPath(loc, dir, goal, Some(con.adjacentOf(currentTrap.get)))
            case None => 
                false
        }
    }


    /**
     * If i would walk from my position onward in a direction, check where i would get to a border
     */
    def farthestPoint(pos: Coord2D, dir: Coord2D, current: Option[Trapezoid] = None): Option[Coord2D] = {
        val currentTrap = if (current.isDefined) current else trapezoidFor(pos)
        if (!currentTrap.isDefined) return None
        // get the trapezoid that will be the last one before we collide with a border
        def lastTrap(pos: Coord2D, dir: Coord2D, trap: Trapezoid): (Trapezoid, Coord2D) = {
            trap.crossedConnection(pos, dir) match {
                case Some((con, loc)) => lastTrap(loc, dir, con.adjacentOf(trap))
                case None           => (trap, pos)
            }
        }
        val (newTrap, newPos) = lastTrap(pos, dir, currentTrap.get)
        newTrap.crossedBorder(newPos, dir)
    }
}


/**
 * Companion with convenience methods.
 */
object PathingMap {
    // deserialization
    import Edge._
    import Trapezoid._
    implicit def simplePathingMapFactory            = factory[SimplePathingMap]             ('fromJson)


    /**
     * Try to apply from file (can fail with None)
     */
    def apply(file: String): Option[PathingMap] = {
        Try(Json.fromJson[SimplePathingMap](Json.parse(Source.fromFile(file).mkString.trim)).get) match {
            case pmap: Success[_] => Some(PathingMap.sophisticate(pmap.get))
            case pmap: Failure[_] => None
        }
    }


    /**
     * Creates associations from the IDs used in the simple versions of connections
     */
    private def sophisticate(simple: SimplePathingMap) = {
        val traps: Map[Int, Trapezoid] = 
            (for (t <- simple.trapezoids) yield 
                (t.id -> Trapezoid.sophisticate(t)))
            .toMap
        val conns: Set[HorizontalConnection] =
            (for (c <- simple.connections) yield 
                HorizontalConnection(c.west, c.east, c.y, traps(c.north), traps(c.south)))
            .toSet

        // update the connections on the trapezoids
        conns foreach { c => 
            c.northern.connections = c :: c.northern.connections
            c.southern.connections = c :: c.southern.connections 
        }

        new PathingMap(traps.values.toList, conns)
    }
}