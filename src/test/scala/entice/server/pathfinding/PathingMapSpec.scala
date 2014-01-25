/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.pathfinding

import entice.server.test._
import entice.protocol.Coord2D

import org.scalatest._
import org.scalatest.matchers._


class PathingMapSpec 
    extends WordSpec 
    with MustMatchers
    with OneInstancePerTest  {

    "A pathing-map" must {


        "load from a given file" in {
            File("pathingMapSpecTestFile").write(PathingMapSpec.sampleMap)

            // load it
            val map = PathingMap.fromFile("pathingMapSpecTestFile")
            map must not be(None)

            // delete the file
            File("pathingMapSpecTestFile").delete
        }


        "get the trapezoid of a position" in {
            // load the pmap
            val map = PathingMap.fromString(PathingMapSpec.sampleMap)
            map must not be(None)
            val trap1 = map.get.connections.head.northern
            val trap2 = map.get.connections.head.southern

            // select a valid position
            val validPos = Coord2D(9, 9) // trap 1, bottom right corner

            // check if it returns the right trapezoid
            map.get.trapezoidFor(validPos) must be(Some(trap1))

            // select an invalid position
            val invalidPos = Coord2D(-7, -8) // top right

            // check if it returns None
            map.get.trapezoidFor(invalidPos) must be(None)
        }


        "check if there is a walkable line from a position to a trapezoid" in {
            //load the map
            val map = PathingMap.fromString(PathingMapSpec.sampleMap)
            map must not be(None)
            val trap1 = map.get.connections.head.northern
            val trap2 = map.get.connections.head.southern

            // select a valid position and direction that points
            // to a connection to the other trapezoid
            val pos1 = Coord2D(2, 2)  // T1, south-east
            val dir1 = Coord2D(0, 1) // pointing directly south

            // check if we can walk to trap 2
            map.get.hasDirectPath(pos1, dir1, trap2, Some(trap1)) must be(true)

            // select a valid position and a direction that points
            // to a border of this trapezoid
            val pos2 = Coord2D(2, 2)  // T1, south-east
            val dir2 = Coord2D(0, -1) // pointing directly north
                 
            // check if we can walk to trap 2
            map.get.hasDirectPath(pos2, dir2, trap2, Some(trap1)) must be(false)

            // select an invalid position
            val pos3 = Coord2D(2, -15) // outside T1, north-east
            val dir3 = Coord2D(0, 1)   // pointing directly south

            map.get.hasDirectPath(pos3, dir3, trap1) must be(false)
            map.get.hasDirectPath(pos3, dir3, trap2) must be(false)
            map.get.hasDirectPath(pos3, dir3, trap1, Some(trap1)) must be(false)
            map.get.hasDirectPath(pos3, dir3, trap2, Some(trap1)) must be(false)
        }


        "get the farthest point reachable from a position" in {
            // load the pmap
            val map = PathingMap.fromString(PathingMapSpec.sampleMap)
            map must not be(None)
            val trap1 = map.get.connections.head.northern
            val trap2 = map.get.connections.head.southern

            // select a valid position and direction that points
            // to a border in the current trapezoid
            val pos1 = Coord2D(2, 2)  // T1, south-east
            val dir1 = Coord2D(0, -1) // pointing directly north
            val intersect1 = Coord2D(2, -10)

            // check if it returns the correct border-point
            map.get.farthestPosition(pos1, dir1, Some(trap1)) must be(Some(intersect1))

            // select a valid position and direction that points
            // to a border in another trapezoid (trough connections to it)
            val pos2 = Coord2D(2, 2) // T1, south-east
            val dir2 = Coord2D(0, 1) // pointing directly south
            val intersect2 = Coord2D(2, 20)

            // check if it returns the correct border-point
            map.get.farthestPosition(pos2, dir2, Some(trap1)) must be(Some(intersect2))

            // select an invalid position
            val pos3 = Coord2D(2, -15) // outside T1, north-east
            val dir3 = Coord2D(0, 1)   // pointing directly south
            val intersect3 = Coord2D(2, -10)

            map.get.farthestPosition(pos3, dir3) must be(None)
            map.get.farthestPosition(pos3, dir3, Some(trap1)) must be(None)
        }


        "get the next valid position, if any" in {
            // load the pmap
            val map = PathingMap.fromString(PathingMapSpec.sampleMap)
            map must not be(None)
            val trap1 = map.get.connections.head.northern
            val trap2 = map.get.connections.head.southern

            // select two positions that are both inside trap1
            val posCur1    = Coord2D(2, 2)  // T1, south-east
            val posNext1   = Coord2D(5, -5) // T1, north-east
            val posExpect1 = Coord2D(5, -5) // see above

            // check if it returns the correct border-point
            map.get.nextValidPosition(posCur1, posNext1, Some(trap1)) must be(Some(posExpect1))

            // select a position that is inside, and a position that is
            // outside, with the next valid pos in between
            val posCur2    = Coord2D(-1, -9) // T1, north-west
            val posNext2   = Coord2D(1, -11) // T1, north-east
            val posExpect2 = Coord2D(0, -10) // in between, on border

            // check if it returns the correct border-point
            map.get.nextValidPosition(posCur2, posNext2, Some(trap1)) must be(Some(posExpect2))

            // select an invalid position
            val posCur3    = Coord2D(-1, 21) // outside T2, south-west
            val posNext3   = Coord2D(1, 19)  // T2, south-east

            map.get.nextValidPosition(posCur3, posNext3) must be(None)
            map.get.nextValidPosition(posCur3, posNext3, Some(trap2)) must be(None)
        }
    }
}


/**
 * Layout of the sample pathing map:
 *
 *                    N
 *
 *                    |
 *          -5        |        5
 *            --------|--------
 *           /        |-10     \
 *          /         |         \
 *         /          |          \
 *        /           |           \   x-axis
 *--------------------+--------------------> E
 *      /             |             \
 *     /              |              \
 *    /         T1    |               \
 *   /-10             |10           10 \
 *  ------------------|------------------
 *   \_               |               _/
 *     \_       T2    |             _/
 *       \_           |           _/
 *         \_         |20       _/
 *           ---------|---------
 *         -5         |         5
 *                    v y-axis
 *
 *                    S
 *
 */
object PathingMapSpec {
    val sampleMap = """
{
    "trapezoids":
    [
        {
            "id":0,
            "north":
            {
                "west":-5,
                "east":5,
                "y":-10
            },
            "south":
            {
                "west":-10,
                "east":10,
                "y":10
            },
            "west":
            {
                "north":{"x":-5,"y":-10},
                "south":{"x":-10,"y":10}
            },
            "east":
            {
                "north":{"x":5,"y":-10},
                "south":{"x":10,"y":10}
            }
        },
        {
            "id":1,
            "north":
            {
                "west":-10,
                "east":10,
                "y":10
            },
            "south":
            {
                "west":-5,
                "east":5,
                "y":20
            },
            "west":
            {
                "north":{"x":-10,"y":10},
                "south":{"x":-5,"y":20}
            },
            "east":
            {
                "north":{"x":10,"y":10},
                "south":{"x":5,"y":20}
            }
        }
    ],
    "connections":
    [
        {
            "id":0,
            "west":-10,
            "east":10,
            "y":10,
            "north":0,
            "south":1
        }
    ]
}"""
}