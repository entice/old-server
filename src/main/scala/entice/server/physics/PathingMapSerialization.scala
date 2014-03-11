/**
 * For copyright information see the LICENSE document.
 */

package entice.server.physics

import Geometry._
import entice.protocol.Coord2D
import play.api.libs.json._
import info.akshaal.json.jsonmacro._
import scala.io._
import scala.util._


/** Parse the old pathingmap format */
case class PathingMap(
    trapezoids: Array[Trapezoid], 
    connections: Array[HorizontalConnection])

case class Trapezoid(
    id: Int,
    north: HorizontalBorder, 
    south: HorizontalBorder,
    west: VerticalBorder, 
    east: VerticalBorder)

case class HorizontalBorder(
    west: Float, 
    east: Float, 
    y: Float)       

case class VerticalBorder(
    north: Coord2D, 
    south: Coord2D)

case class HorizontalConnection(
    west: Float, 
    east: Float, 
    y: Float,
    north: Int, 
    south: Int)


/** Companion with convenience methods. */
object PathingMap {
    // deserialization
    import entice.protocol.Utils._
    implicit def horizontalBorderFactory      = factory[HorizontalBorder]     ('fromJson)
    implicit def horizontalConnectionFactory  = factory[HorizontalConnection] ('fromJson)
    implicit def verticalBorderFactory        = factory[VerticalBorder]       ('fromJson)
    implicit def trapezoidFactory             = factory[Trapezoid]            ('fromJson)
    implicit def pathingMapFactory            = factory[PathingMap]           ('fromJson)

    
    /** Try to load from a JSON string. (can fail with None) */
    def fromString(jsonMap: String): Option[CollisionMesh] = {
        Try(Json.fromJson[PathingMap](Json.parse(jsonMap)).get) match {
            case pmap: Success[_] => Some(toCollisionMesh(pmap.get))
            case pmap: Failure[_] => None
        }
    }


    /** Try to load from a JSON file. (can fail with None) */
    def fromFile(file: String): Option[CollisionMesh] = {
        Try(Source.fromFile(file).mkString.trim) match {
            case pmap: Success[_] => fromString(pmap.get)
            case pmap: Failure[_] => None
        }
    }


    /** Creates associations from the IDs used in the simple versions of connections */
    private def toCollisionMesh(pmap: PathingMap) = {
        // generate an association of trapId -> quad
        val quads: Map[Int, Quadliteral] = 
            (pmap.trapezoids map { t: Trapezoid => (t.id -> toQuadliteral(t)) }).toMap

        val conns: Set[Connection with Segment2D] =
            (pmap.connections map { c: HorizontalConnection => toConnection(c, quads) }).toSet

        // update the connections on the trapezoids
        conns map connect

        new CollisionMesh(quads.values.toList)
    }

    private def toQuadliteral(t: Trapezoid): Quadliteral = {
        Quadliteral(
            if (t.west.south == t.west.north) new Edge(t.west.south, t.west.north) with Point2D
            else                              new Edge(t.west.south, t.west.north) with Segment2D,
            if (t.west.north == t.east.north) new Edge(t.west.north, t.east.north) with Point2D
            else                              new Edge(t.west.north, t.east.north) with Segment2D,
            if (t.east.north == t.east.south) new Edge(t.east.north, t.east.south) with Point2D
            else                              new Edge(t.east.north, t.east.south) with Segment2D,
            if (t.east.south == t.west.south) new Edge(t.east.south, t.west.south) with Point2D
            else                              new Edge(t.east.south, t.west.south) with Segment2D)
    }

    private def toConnection(c: HorizontalConnection, quads: Map[Int, Quadliteral]): Connection with Segment2D = {
        new Connection(
            Coord2D(c.west, c.y),
            Coord2D(c.east, c.y),
            quads(c.north),
            quads(c.south)) with Segment2D
    }

    private def connect(c: Connection with Segment2D) {
        val q1 = c.quad1
        val q2 = c.quad2

        q1.edges find (_.location(c.p1) == OnLine) match {
            case Some(_) => q1.connections = c :: q1.connections
            case _       => throw new Exception("Connection not linked to quadliteral! " + c + q1)
        }

        q2.edges find (_.location(c.p1) == OnLine) match {
            case Some(_) => q2.connections = c :: q2.connections
            case _       => throw new Exception("Connection not linked to quadliteral! " + c + q1)
        }
    }
}