/**
 * For copyright information see the LICENSE document.
 */

package entice.server.pathfinding

import entice.protocol.Coord2D
import scala.util._


/**
 * Geometry utilities
 */
object Geometry {


    /**
     * Extends floats :3
     */
    implicit class RichFloat(a: Float) {
        def within(b: Float, c:Float) = (b min c) <= a && a <= (b max c)
    }
    

    /**
     * Extends Coord2D with some point functionality
     */
    implicit class RichCoord2D(coord: Coord2D) {
        def toLine(other: Coord2D)  = new Line2D(coord, other)
        def alignWith(dir: Coord2D) = {
            require(dir != Coord2D(0, 0))
            new Line2D(coord, coord + dir)
        }
    }


    /**
     * Extends Coord2D with some vector functionality
     */
    implicit class VectorizedCoord2D(coord: Coord2D) {
        def +(other: Coord2D) = Coord2D(coord.x + other.x, coord.y + other.y)
        def -(other: Coord2D) = Coord2D(coord.x - other.x, coord.y - other.y)
        def *(value: Float)   = Coord2D(coord.x * value, coord.y * value)
        def /(value: Float)   = Coord2D(coord.x / value, coord.y / value)
        def o(other: Coord2D) = coord.x * other.x + coord.y * other.y
        def len               = Math.sqrt(coord.x * coord.x + coord.y * coord.y).toFloat
        def pointsNorth       = coord.y < 0
        def pointsSouth       = coord.y > 0
        def pointsWest        = coord.x < 0
        def pointsEast        = coord.x > 0

        def unit              = {
            require(coord != Coord2D(0, 0))
            coord / coord.len
        }
    }


    /**
     * Infinite line
     */
    class Line2D(p1: Coord2D, p2: Coord2D) {
        require(p1 != p2)

        val a = p1.y - p2.y;
        val b = p2.x - p1.x;
        val c = -(p1.x - p2.x) * p1.y - (p2.y - p1.y) * p1.x;

        /**
         * Returns the intersection point if any
         */
        def intersect(other: Line2D): Option[Coord2D] = {
            val det = a * other.b - b * other.a;
            // is parallel?
            if (det == 0) return None;
            Some(Coord2D(
                (c * other.b - b * other.c) / det,
                (a * other.c - c * other.a) / det))
        }


        /**
         * Do i cross the line when i walk from a point in a direction?
         * Does fail when i'm standing on the line (use intersect for that)
         */
        def walkOver(pos: Coord2D, dir: Coord2D): Option[Coord2D] = {
            require(dir != Coord2D(0, 0))

            val line = pos.alignWith(dir)
            intersect(line) match {
                // if we walk a bit in the dir, do we get closer to the intersection point?
                case Some(loc) if ((pos - loc).len >= ((pos + dir) - loc).len) =>
                    Some(loc)
                case _ => 
                    None
            }
        }


        /** 
         * Returns the relative loction of a point to this segment
         *
         * Note that the line is regarded as with direction:
         *  p1 --> p2
         *
         * The result of this calculation determins if the point is
         *  - to the left:  result < 0
         *  - on the line:  result = 0
         *  - to the right: result > 0
         */
        def location(pos: Coord2D) = (p2 - p1) o (pos - p2)
    }


    /**
     * Line segment
     */
    class Segment2D(p1: Coord2D, p2: Coord2D) extends Line2D(p1, p2){
        /**
         * Returns the intersection point if any
         */
        override def intersect(other: Line2D): Option[Coord2D] = {
            super.intersect(other) match {
                case Some(loc) if (loc.x.within(p1.x, p2.x) && loc.y.within(p1.y, p2.y)) => 
                    Some(loc)
                case _ => 
                    None
            }
        }


        /**
         * Distance to a point
         */
        def distance(pos: Coord2D): Float = {
            val v = p1 - p2
            val w = pos - p2
            val (c1, c2) = (w o v, v o v)
            if (c1 <= 0)  return (pos - p2).len
            if (c2 <= c1) return (pos - p1).len
            return (pos - (p2 + v * (c1 / c2))).len
        }
    }
}