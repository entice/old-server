/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world

import entice.protocol.Coord2D
import play.api.libs.json._
import info.akshaal.json.jsonmacro._
import scala.io._
import scala.util._


/**
 * Consists of the walkable trapezoids and their connections.
 */
class PathingMap(
    val trapezoids: Set[Trapezoid],
    val connections: Set[HorizontalConnection])

object PathingMap {
    // deserialization
    import entice.protocol.Utils._    
    implicit def horizontalBorderFactory            = factory[HorizontalBorder]             ('fromJson)
    implicit def simpleHorizontalConnectionFactory  = factory[SimpleHorizontalConnection]   ('fromJson)
    implicit def verticalBorderFactory              = factory[VerticalBorder]               ('fromJson)
    implicit def trapezoidFactory                   = factory[Trapezoid]                    ('fromJson)
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
        val traps: Map[Int, Trapezoid] = (for (t <- simple.trapezoids) yield (t.id -> t)).toMap
        val conns: Set[HorizontalConnection] =
            (for (c <- simple.connections) yield 
                HorizontalConnection(c.west, c.east, c.y, traps(c.north), traps(c.south)))
            .toSet
        new PathingMap(traps.values.toSet, conns)
    }
}


/**
 * Simplified non-associative version, for serialization purposes.
 */
case class SimplePathingMap    (trapezoids: Array[Trapezoid],
                                connections: Array[SimpleHorizontalConnection])


/**
 * This is aligned with the y-axis (north - south):
 *
 *      (N)
 *       |
 *       |     /---------\
 *       |    /           \
 *       |   /             \
 *       |  /_______________\
 *       |
 * (W) ------------------------> (E)
 *       |
 *       v
 *      (S)
 *
 */
case class Trapezoid           (id: Int,
                                north: HorizontalBorder, 
                                south: HorizontalBorder,
                                west: VerticalBorder, 
                                east: VerticalBorder)


/**
 * This is a horizontally aligned line
 */
sealed trait HorizontalEdge {
    def west: Float
    def east: Float
    def y:    Float
}
case class HorizontalBorder    (west: Float, east: Float, y: Float) extends HorizontalEdge
case class HorizontalConnection(west: Float, east: Float, y: Float,
                                northern: Trapezoid,
                                southern: Trapezoid)                extends HorizontalEdge
case class SimpleHorizontalConnection
                                (west: Float, east: Float, y: Float,
                                id: Int, // TODO: deprecated
                                north: Int,
                                south: Int)                         extends HorizontalEdge


/**
 * This is just NOT horizontally aligned, but not necessarily
 * vertical aligned! Hence this line is given by two points.
 */
sealed trait VerticalEdge {
    def north: Coord2D
    def south: Coord2D
}
case class VerticalBorder      (north: Coord2D, south: Coord2D)     extends VerticalEdge