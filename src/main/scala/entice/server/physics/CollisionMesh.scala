/**
 * For copyright information see the LICENSE document.
 */

package entice.server.physics

import Geometry._
import entice.protocol.Coord2D
import scala.util.control.Breaks._


case class CollisionMesh(
    quads: List[Quadliteral]) {
    
    /** Checks which quadliteral the position is in */
    def quadliteralFor(pos: Coord2D): Option[Quadliteral] = quads.find(_.contains(pos))

    /** If i would walk from my position onward in a direction, check where i would get to a border */
    def farthestPosition(pos: Coord2D, dir: Coord2D, current: Option[Quadliteral] = None): Option[Coord2D] = {
        val curQuad = current orElse quadliteralFor(pos)
        if (!curQuad.isDefined) return None
        if (!curQuad.get.contains(pos)) return None

        var lastQuad = curQuad.get
        var lastPos  = pos

        // special case: on a connection
        if (curQuad.get.isOnConnection(pos)) {
            // get the offending connection
            val conn = curQuad.get.crossedConnection(pos, dir, true).get._1
            // try find a new connection that we are poking through
            conn.quad1.crossedConnection(pos, dir, false) orElse
            conn.quad2.crossedConnection(pos, dir, false) match {
                // we found one
                case Some((conn, p)) =>
                    lastQuad = conn.adjacentOf(lastQuad)
                    lastPos  = p
                // we found none. in this case find the next best edge
                // which is either some that we can walk over, or our pos
                // if we are wlking against the wall
                case _ =>
                    conn.quad1.crossedEdge(pos, dir, false) orElse
                    conn.quad2.crossedEdge(pos, dir, false) match {
                        case Some((edge, p)) => return Some(p)
                        case _               => return Some(pos)
                    }
            }
        }

        // go through the connection and use them to iterate through the arrays
        breakable { while (true) {
            lastQuad.crossedConnection(lastPos, dir, false) match {
                case Some((conn, p)) => 
                    lastQuad = conn.adjacentOf(lastQuad)
                    lastPos  = p
                case _ => break
            }
        }}

        // finally look for the actual edge that we collide with
        lastQuad.crossedEdge(lastPos, dir, false) orElse
        lastQuad.crossedEdge(lastPos, dir, true) match {
            case Some((edge, p)) => Some(p)
            case _               => None
        }
    }

}

case class Quadliteral(
    e1: Edge with Locateable, 
    e2: Edge with Locateable,
    e3: Edge with Locateable, 
    e4: Edge with Locateable,
    var connections: List[Connection with Locateable] = Nil) {

    val edges = e1 :: e2 :: e3 :: e4 :: Nil

    def contains(pos: Coord2D) : Boolean = {
        edges.foldLeft(true) { (contained, e) =>
            contained && (e.location(pos) == ToRight || e.location(pos) == OnLine)
        }
    }

    def crossedEdge(pos: Coord2D, dir: Coord2D, inclusive: Boolean) : Option[(Edge, Coord2D)] = {
        edges.foreach { e =>
            e.walkOver(pos, dir, inclusive) match {
                case Some(p: Coord2D) => return Some((e, p)) 
                case _                => None
            }
        }
        None
    }

    def crossedConnection(pos: Coord2D, dir: Coord2D, inclusive: Boolean) : Option[(Connection, Coord2D)] = {
        connections.foreach { c =>
            c.walkOver(pos, dir, inclusive) match {
                case Some(p: Coord2D) => return Some((c, p)) 
                case _                => None
            }
        }
        None
    }

    def isOnConnection(pos: Coord2D) : Boolean = {
        connections.find(_.location(pos) == OnLine).isDefined
    }
}

case class Edge(
    p1: Coord2D, 
    p2: Coord2D) 

case class Connection(
    p1: Coord2D, 
    p2: Coord2D,
    quad1: Quadliteral, 
    quad2: Quadliteral) {
    def adjacentOf(me: Quadliteral) = if (me == quad1) quad2 else quad1
    override def toString = f"Connection($p1%s,$p2%s,...)"
}