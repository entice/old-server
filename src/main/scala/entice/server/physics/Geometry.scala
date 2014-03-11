/**
 * For copyright information see the LICENSE document.
 */

package entice.server.physics

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
         * @param inclusive This determins if the function also considers the line that
         * i may be standing on or not.
         */
        def walkOver(pos: Coord2D, dir: Coord2D, inclusive: Boolean): Option[Coord2D]

        /** 
         * Returns the relative location of a point to this segment
         *
         * Note that a line is regarded as with direction:
         *  p1 --> p2
         *
         * For point-point relationships, this is always 'OnLine'
         */
        def location(pos: Coord2D): RelativeLocation
    }

    sealed trait RelativeLocation
    case object ToLeft  extends RelativeLocation
    case object ToRight extends RelativeLocation
    case object OnLine  extends RelativeLocation


    /**
     * Extends floats :3
     */
    implicit class RichFloat(a: Float) {
        def ~==(other: Float)         = Math.abs(a - other) < RichFloat.EPSILON
        def ~!=(other: Float)         = !(a ~== other)
        def ~<=(other: Float)         = (a < other) || (a ~== other)
        def ~>=(other: Float)         = (a > other) || (a ~== other)
        def within(b: Float, c:Float) = ((b min c) ~<= a) && (a ~<= (b max c))
    }

    object RichFloat {
        val EPSILON = 0.0001F
    }
    

    /**
     * Extends Coord2D with some point functionality
     */
    implicit class RichCoord2D(coord: Coord2D) {
        def toLine(other: Coord2D)  = new Line2D { def p1 = coord; def p2 = other }
        def alignWith(dir: Coord2D) = {
            require(dir != Coord2D(0, 0))
            toLine(coord + dir)
        }
    }


    /**
     * Extends Coord2D with some vector functionality
     */
    implicit class VectorizedCoord2D(coord: Coord2D) {
        def +(other: Coord2D)   = Coord2D(coord.x + other.x, coord.y + other.y)
        def -(other: Coord2D)   = Coord2D(coord.x - other.x, coord.y - other.y)
        def *(value: Float)     = Coord2D(coord.x * value, coord.y * value)
        def /(value: Float)     = Coord2D(coord.x / value, coord.y / value)
        def o(other: Coord2D)   = coord.x * other.x + coord.y * other.y
        def ~==(other: Coord2D) = (coord.x ~== other.x) && (coord.y ~== other.y)
        def ~!=(other: Coord2D) = (coord.x ~!= other.x) || (coord.y ~!= other.y)
        def len                 = Math.abs(Math.sqrt((coord.x * coord.x) + (coord.y * coord.y)).toFloat)
        def pointsNorth         = coord.y > 0
        def pointsSouth         = coord.y < 0
        def pointsWest          = coord.x < 0
        def pointsEast          = coord.x > 0
        def unit: Coord2D       = if (coord == Coord2D(0, 0)) coord.copy() else coord / coord.len  
        
        def inRect(p1: Coord2D, p2: Coord2D) = {
            (coord.x.within(p1.x, p2.x) && coord.y.within(p1.y, p2.y))
        }
    }


    /**
     * Point (a line segment that has length = 0)
     */
    trait Point2D extends Locateable {
        def p1 : Coord2D

        def location(pos: Coord2D): RelativeLocation = OnLine

        def intersect(other: Line2D): Option[Coord2D] = {
            if (other.isOnLine(p1)) Some(p1) else None
        }

        def walkOver(pos: Coord2D, dir: Coord2D, inclusive: Boolean): Option[Coord2D] = {
            // failcheck
            if (dir ~== Coord2D(0, 0)) { return None }

            val line = pos.alignWith(dir)
            intersect(line) match {
                // if we walk a bit in the dir, do we get closer to the intersection point?
                case Some(loc) 
                     if ((loc - pos).unit ~== dir.unit) => Some(loc)
                case _                                  => None
            }
        }
    }


    /**
     * Wrapper class for java.awt.geom.Line2D stuff
     */
    trait Line2D {
        def p1: Coord2D
        def p2: Coord2D

        def intersectWith(line: Line2D): Option[Coord2D] = {
            val line1 = this.linearEquation
            val line2 = line.linearEquation

            var x = 0F
            var y = 0F
            if (line1._1.isInfinite()) {    // line 1 is a vertical straight line
                x = this.p1.x
                y = line2._1*x + line2._2
            } else if (line2._1.isInfinite()) { // line 2 is a vertical straight line
                x = line.p1.x
                y = line1._1*x + line1._2
            } else {
                x = (line2._2 - line1._2) / (line1._1 - line2._1)
                y = line1._1 * x + line1._2
            }

            if (x.isInfinite() || y.isInfinite()) { None }
            else                                  { Some(Coord2D(x, y)) }
        }

        def linearEquation(): (Float, Float) = {
            val k = (p2.y-p1.y)/(p2.x-p1.x)
            val d = p1.y - k*p1.x
            (k, d)
        }

        def isOnLine(p: Coord2D) = {
            val line = this.linearEquation
            var error = 0F
            if (line._1.isInfinite()) { // line is a vertical line
                error = this.p1.x - p.x
            } else if (line._2.isInfinite()) {  // lines is a horizontal line
                error = this.p1.y - p.y
            } else {
                error = p.y - (line._1 * p.x + line._2)
            }
            Math.abs(error) < RichFloat.EPSILON
        }
    }


    /**
     * Line segment
     */
    trait Segment2D extends Line2D with Locateable {
        def p1 : Coord2D
        def p2 : Coord2D

        def intersect(other: Line2D): Option[Coord2D] = {
            intersectWith(other) match {
                case Some(loc) 
                     if loc.inRect(p1, p2) => Some(loc)
                case _                     => None
            }
        }

        def walkOver(pos: Coord2D, dir: Coord2D, inclusive: Boolean): Option[Coord2D] = {
            // failcheck
            if (dir ~== Coord2D(0, 0)) { return None }

            // we do not need to walk, since we are there already
            if (inclusive && isOnLine(pos)) { return Some(pos) }

            val line = pos.alignWith(dir)
            intersect(line) match {
                // if we walk in the dir, do we get closer to the intersection point?
                case Some(loc) 
                     if ((loc - pos).unit ~== dir.unit) => Some(loc)
                case _                                  => None
            }
        }

        def location(pos: Coord2D): RelativeLocation = {
            if (isOnLine(pos)) return OnLine

            val v1 = (p2 - p1)
            val v2 = (pos - p2)
            (v1.x * v2.y - v1.y * v2.x) match {
                case loc if loc <   0 => ToRight
                case loc if loc ~== 0 => OnLine // should be caught above, but well
                case loc if loc >   0 => ToLeft
            }
        }

        // TODO: delete this?
        // def distance(pos: Coord2D): Float = {
        //     val v = p1 - p2
        //     val w = pos - p2
        //     val (c1, c2) = (w o v, v o v)
        //     if (c1 <= 0)  return (pos - p2).len
        //     if (c2 <= c1) return (pos - p1).len
        //     return (pos - (p2 + v * (c1 / c2))).len
        // }
    }
}