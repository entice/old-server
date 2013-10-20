/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world

import entice.protocol._


/**
 * Consists of the walkable trapezoids and their connections.
 */
case class PathingMap          (trapezoids: Map[Int, Trapezoid],
                                connections: Set[HorizontalConnection])


/**
 * Simplified non-associative version, for serialization purposes.
 */
case class SimplePathingMap    (trapezoids: List[Trapezoid],
                                connections: List[HorizontalConnection]) {
    def sophisticate = {
        PathingMap(
            trapezoids  = (for (t <- trapezoids) yield (t.id -> t)).toMap,
            connections = connections.toSet)
    }
}


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
                                northernTrapezoid: Int,
                                southernTrapezoid: Int)             extends HorizontalEdge


/**
 * This is just NOT horizontally aligned, but not necessarily
 * vertical aligned! Hence this line is given by two points.
 */
sealed trait VerticalEdge {
    def north: Coord2D
    def south: Coord2D
}
case class VerticalBorder      (north: Coord2D, south: Coord2D)     extends VerticalEdge