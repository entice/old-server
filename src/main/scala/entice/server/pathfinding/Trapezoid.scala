/**
 * For copyright information see the LICENSE document.
 */

package entice.server.pathfinding

import entice.protocol.Coord2D
import play.api.libs.json._
import info.akshaal.json.jsonmacro._

import Geometry._


/**
 * Simplified non-associative version, for serialization purposes.
 * TODO:
 *  delete west/east
 */
case class SimpleTrapezoid(
    id: Int,
    north: HorizontalBorder, 
    south: HorizontalBorder,
    west: VerticalBorder, 
    east: VerticalBorder)


/**
 * Its northern and southern borders are aligned with the x-axis (west-east):
 *
 *      (N)
 *       ^
 *       |     /---------\
 *       |    /           \
 *       |   /             \
 *       |  /_______________\
 *       |
 * (W) --+---------------------> (E)
 *       |
 *      (S)
 *
 */
case class Trapezoid(
    north: HorizontalBorder, 
    south: HorizontalBorder,
    west: VerticalBorder, 
    east: VerticalBorder,
    var connections: List[HorizontalConnection] = Nil) {


    /**
     * Returns true if this includes a specified point
     */
    def contains(pos: Coord2D) = {
        ((north.location(pos) == ToRight) || (north.location(pos) == OnLine)) && // W -> E
        ((south.location(pos) == ToLeft)  || (south.location(pos) == OnLine)) && // W -> E
        ((west.location(pos)  == ToRight) || (west.location(pos)  == OnLine)) && // S -> N
        ((east.location(pos)  == ToLeft)  || (east.location(pos)  == OnLine))    // S -> N
    }


    /**
     * Do i cross the border of my trap somewhere?
     */
    def crossedBorder(pos: Coord2D, dir: Coord2D): Option[Coord2D] = {
        if (!contains(pos)) return None
        // first check all borders without the one i'm standing on
        // then also check the ones that i'm standing on
        north.crossedExclusive(pos, dir) orElse
        south.crossedExclusive(pos, dir) orElse
        west.crossedExclusive(pos, dir) orElse
        east.crossedExclusive(pos, dir) orElse
        north.crossedInclusive(pos, dir) orElse
        south.crossedInclusive(pos, dir) orElse
        west.crossedInclusive(pos, dir) orElse
        east.crossedInclusive(pos, dir)
    }


    /**
     * Do i cross a connection to another trap somewhere?
     * Does not consider the connection that i'm standing on.
     */
    def crossedConnection(pos: Coord2D, dir: Coord2D): Option[(HorizontalConnection, Coord2D)] = {
        if (!contains(pos)) return None
        for (c <- connections) {
            val loc = c.crossedExclusive(pos, dir)
            if (loc.isDefined) return Some((c, loc.get))
        }
        None
    }
}


/**
 * Companion with deserialisation stuff
 */
object Trapezoid {
    import Edge._
    implicit def simpleTrapezoidFactory = factory[SimpleTrapezoid] ('fromJson)

    def sophisticate(simple: SimpleTrapezoid): Trapezoid = {
        Trapezoid(simple.north, simple.south, simple.west, simple.east)
    }
}