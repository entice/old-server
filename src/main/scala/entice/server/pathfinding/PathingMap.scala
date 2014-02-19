/**
 * For copyright information see the LICENSE document.
 */

package entice.server.pathfinding

import entice.protocol.Coord2D
import entice.server.test._
import play.api.libs.json._
import info.akshaal.json.jsonmacro._
import scala.io._
import scala.util._

import Geometry._


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
    val connections: Set[HorizontalConnection]) extends SVGConversion{


    /**
     * Checks which trapezoid the position is in
     */
    def trapezoidFor(pos: Coord2D): Option[Trapezoid] = {
        trapezoids.foreach { trap =>
            if (trap.contains(pos)) return Some(trap)
        }
        None
    }


    /**
     * Checks if there is a linear path from a position in a direction
     * to a certain trapezoid
     */
    def hasDirectPath(pos: Coord2D, dir: Coord2D, goal: Trapezoid, current: Option[Trapezoid] = None): Boolean = {
        val currentTrap = if (current.isDefined) current else trapezoidFor(pos)
        if (!currentTrap.isDefined) return false
        if (!currentTrap.get.contains(pos)) return false
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
    def farthestPosition(pos: Coord2D, dir: Coord2D, current: Option[Trapezoid] = None): Option[Coord2D] = {
        val currentTrap = if (current.isDefined) current else trapezoidFor(pos)
        if (!currentTrap.isDefined) return None
        if (!currentTrap.get.contains(pos)) return None
        // get the trapezoid that will be the last one before we collide with a border
        def lastTrap(curPos: Coord2D, curDir: Coord2D, curTrap: Trapezoid): (Trapezoid, Coord2D) = {
            curTrap.crossedConnection(curPos, curDir) match {
                case Some((con, loc)) => lastTrap(loc, curDir, con.adjacentOf(curTrap))
                case None             => (curTrap, curPos)
            }
        }
        val (newTrap, newPos) = lastTrap(pos, dir, currentTrap.get)
        newTrap.crossedBorder(newPos, dir)
    }


    /**
     * Is our next position valid?
     */
     def nextValidPosition(pos: Coord2D, nextPos: Coord2D, current: Option[Trapezoid] = None): Option[Coord2D] = {
        val currentTrap = if (current.isDefined) current else trapezoidFor(pos)
        if (!currentTrap.isDefined) return None
        if (!currentTrap.get.contains(pos)) return None

        val farPos = farthestPosition(pos, (nextPos - pos), currentTrap)
        if (!farPos.isDefined) return None
        // our next pos must be between the current and the maximum possible pos to be valid
        // else we return the max possible position
        if (nextPos.x.within(pos.x, farPos.get.x) && nextPos.y.within(pos.y, farPos.get.y)) {
            return Some(nextPos)
        } else  {
            return farPos
        }

     }
}


/**
 * Companion with convenience methods.
 */
object PathingMap {
    // deserialization
    import Edge._
    import Trapezoid._
    implicit def simplePathingMapFactory = factory[SimplePathingMap]('fromJson)


    /**
     * Try to load from a JSON string. (can fail with None)
     */
    def fromString(jsonMap: String): Option[PathingMap] = {
        Try(Json.fromJson[SimplePathingMap](Json.parse(jsonMap)).get) match {
            case pmap: Success[_] => Some(PathingMap.sophisticate(pmap.get))
            case pmap: Failure[_] => None
        }
    }


    /**
     * Try to load from a JSON file. (can fail with None)
     */
    def fromFile(file: String): Option[PathingMap] = {
        Try(Source.fromFile(file).mkString.trim) match {
            case pmap: Success[_] => fromString(pmap.get)
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
                new HorizontalConnection(c.west, c.east, c.y, traps(c.north), traps(c.south)))
            .toSet

        // update the connections on the trapezoids
        conns foreach { c => 
            c.northern.connections = c :: c.northern.connections
            c.southern.connections = c :: c.southern.connections 
        }

        new PathingMap(traps.values.toList, conns)
    }
}

trait SVGConversion {
    this: PathingMap =>

    def width     = maxX - minX
    def height    = maxY - minY
    val header    = "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.0\" width=\"" + width + "\" height=\"" + height + "\">"
    val footer    = "</svg>"

    // points need to be abc.def,klm.mnop and separated by space
    val polyStart = "<polygon style=\"fill:none;stroke:#000000;stroke-width:1.5;stroke-miterlimit:10\" points=\""
    val polyEnd   = "\"></polygon>"

    lazy val minX = trapezoids.foldLeft(0F) { (i, t) => 
        if (t.south.west < i) t.south.west else
        if (t.north.west < i) t.north.west else
        i
    }

    lazy val maxX = trapezoids.foldLeft(0F) { (i, t) => 
        if (t.south.east > i) t.south.east else
        if (t.north.east > i) t.north.east else
        i
    }

    lazy val minY = trapezoids.foldLeft(0F) { (i, t) => 
        if (t.north.y < i) t.north.y else i
    }

    lazy val maxY = trapezoids.foldLeft(0F) { (i, t) => 
        if (t.south.y > i) t.south.y else i
    }

    def safeAs(svg: String) {
        val result = new StringBuilder()

        result.append(header)
        trapezoids.foreach { t =>
            // build header
            result.append(polyStart)
            // build graphics
            result.append(t.south.west - minX).append(",").append(t.south.y - minY).append(" ")
            result.append(t.north.west - minX).append(",").append(t.north.y - minY).append(" ")
            result.append(t.north.east - minX).append(",").append(t.north.y - minY).append(" ")
            result.append(t.south.east - minX).append(",").append(t.south.y - minY).append(" ")
            result.append(t.south.west - minX).append(",").append(t.south.y - minY)
            // finish
            result.append(polyEnd)
        }
        result.append(footer)

        File(svg).write(result.toString)
    }

    def safeWithSpawnAs(svg: String, pos: Coord2D) {
        val result = new StringBuilder()

        result.append(header)
        trapezoids.foreach { t =>
            result.append(polyStart)
            result.append(t.south.west - minX).append(",").append(t.south.y - minY).append(" ")
            result.append(t.north.west - minX).append(",").append(t.north.y - minY).append(" ")
            result.append(t.north.east - minX).append(",").append(t.north.y - minY).append(" ")
            result.append(t.south.east - minX).append(",").append(t.south.y - minY).append(" ")
            result.append(t.south.west - minX).append(",").append(t.south.y - minY)
            result.append(polyEnd)
        }
        // build spawn point
        result.append("<circle cx=\"").append(pos.x - minX)
        result.append(     "\" cy=\"").append(pos.y - minY)
        result.append("\" r=\"100\" fill=\"red\" stroke=\"blue\" stroke-width=\"10\"/>")
        // finish
        result.append(footer)

        File(svg).write(result.toString)
    }
}