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
     * Any class that allows comparis to certain geometric objects
     */
    trait Locateable {
        /**
         * Returns the intersection point if any
         */
        def intersect(other: Line2D): Option[Coord2D]

        /**
         * Do i cross the line when i walk from a point in a direction?
         * Does fail when i'm standing on the line (use intersect for that)
         */
        def walkOver(pos: Coord2D, dir: Coord2D): Option[Coord2D]

        /** 
         * Returns the relative location of a point to this segment
         *
         * Note that a line is regarded as with direction:
         *  p1 --> p2
         *
         * For point-point relationships, this is always 'OnLine'
         */
        def location(pos: Coord2D): RelativeLocation

        /**
         * Distance to a point
         */
        def distance(pos: Coord2D): Float
    }

    sealed trait RelativeLocation
    case object ToLeft  extends RelativeLocation
    case object ToRight extends RelativeLocation
    case object OnLine  extends RelativeLocation


    /**
     * Extends floats :3
     */
    implicit class RichFloat(a: Float) {
        def within(b: Float, c:Float) = (b min c) <= a && a <= (b max c)
    }
    

    /**
     * Extends Coord2D with some point functionality
     */
    implicit class RichCoord2D(coord: Coord2D) extends Locateable {
        def toLine(other: Coord2D)  = new Line2D(coord, other)
        def alignWith(dir: Coord2D) = {
            require(dir != Coord2D(0, 0))
            new Line2D(coord, coord + dir)
        }

        def location(pos: Coord2D): RelativeLocation = OnLine
        def distance(pos: Coord2D): Float = (coord - pos).len

        def intersect(other: Line2D): Option[Coord2D] = {
            if (other.location(coord) == OnLine) Some(coord) else None
        }

        def walkOver(pos: Coord2D, dir: Coord2D): Option[Coord2D] = {
            require(dir != Coord2D(0, 0))

            val line = pos.alignWith(dir)
            intersect(line) match {
                // if we walk a bit in the dir, do we get closer to the intersection point?
                case Some(loc) if (Math.abs(((loc - pos).unit - dir.unit).len) < 0.01) =>
                    Some(loc)
                case _ => 
                    None
            }
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
        def len               = Math.abs(Math.sqrt((coord.x * coord.x) + (coord.y * coord.y)).toFloat)
        def pointsNorth       = coord.y > 0
        def pointsSouth       = coord.y < 0
        def pointsWest        = coord.x < 0
        def pointsEast        = coord.x > 0
        def unit: Coord2D     = if (coord == Coord2D(0, 0)) coord.copy() else coord / coord.len  
    }


    /**
     * Infinite line
     */
    class Line2D(p1: Coord2D, p2: Coord2D) extends Locateable {
        require(p1 != p2)

        val a = p1.y - p2.y;
        val b = p2.x - p1.x;
        val c = -(p1.x - p2.x) * p1.y - (p2.y - p1.y) * p1.x;

        def intersect(other: Line2D): Option[Coord2D] = {
            val det = a * other.b - b * other.a;
            // is parallel?
            if (det == 0) return None;
            Some(Coord2D(
                (c * other.b - b * other.c) / det,
                (a * other.c - c * other.a) / det))
        }

        def walkOver(pos: Coord2D, dir: Coord2D): Option[Coord2D] = {
            require(dir != Coord2D(0, 0))

            // we do not need to walk, since we are there already
            if (location(pos) == OnLine) { return Some(pos) }

            val line = pos.alignWith(dir)
            intersect(line) match {
                // if we walk in the dir, do we get closer to the intersection point? (remember that float is unprecise)
                case Some(loc) if (Math.abs(((loc - pos).unit - dir.unit).len) < 0.01)  =>
                    Some(loc)
                case _ => 
                    None
            }
        }

        def location(pos: Coord2D): RelativeLocation = {
            val v1 = (p2 - p1)
            val v2 = (pos - p2)
            (v1.x * v2.y - v1.y * v2.x) match {
                case loc if loc <  0             => ToRight
                case loc if Math.abs(loc) < 0.01 => OnLine
                case loc if loc >  0             => ToLeft
            }
        }

        def distance(pos: Coord2D): Float = {
            val v = p1 - p2
            val w = pos - p2
            val (c1, c2) = (w o v, v o v)
            return (pos - (p2 + v * (c1 / c2))).len
        }
    }


    /**
     * Line segment
     */
    class Segment2D(p1: Coord2D, p2: Coord2D) extends Line2D(p1, p2) with Locateable {
        override def intersect(other: Line2D): Option[Coord2D] = {
            super.intersect(other) match {
                case Some(loc) if (loc.x.within(p1.x, p2.x) && loc.y.within(p1.y, p2.y)) => 
                    Some(loc)
                case _ => 
                    None
            }
        }

        override def distance(pos: Coord2D): Float = {
            val v = p1 - p2
            val w = pos - p2
            val (c1, c2) = (w o v, v o v)
            if (c1 <= 0)  return (pos - p2).len
            if (c2 <= c1) return (pos - p1).len
            return (pos - (p2 + v * (c1 / c2))).len
        }
    }
}
