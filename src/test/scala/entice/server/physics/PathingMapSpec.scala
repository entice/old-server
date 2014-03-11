/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.physics

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
            val quad1 = map.get.quads(0)
            val quad2 = map.get.quads(1)

            // select a valid position
            var validPos = Coord2D(9, -9) // trap 1, bottom right corner
            map.get.quadliteralFor(validPos) must be(Some(quad1))

            validPos = Coord2D(10, -10) // trap 1, bottom right corner
            map.get.quadliteralFor(validPos) must be(Some(quad1))

            // select an invalid position
            val invalidPos = Coord2D(-7, 8) // top right

            // check if it returns None
            map.get.quadliteralFor(invalidPos) must be(None)
        }


        "get the farthest point reachable from a position" in {
            // load the pmap
            val map = PathingMap.fromString(PathingMapSpec.sampleMap)
            map must not be(None)
            val quad1 = map.get.quads(0)
            val quad2 = map.get.quads(1)

            // given
            var pos = Coord2D(2, -2) // T1, south-east
            var dir = Coord2D(0, 1)  // pointing directly north
            var intersect = Coord2D(2, 10)
            // then
            map.get.farthestPosition(pos, dir, Some(quad1)) must be(Some(intersect))

            // given
            pos = Coord2D(2, -2) // T1, south-east
            dir = Coord2D(0, -1) // pointing directly south
            intersect = Coord2D(2, -20)
            // then
            map.get.farthestPosition(pos, dir, Some(quad1)) must be(Some(intersect))

            // given (some corner point, but with weird intersection with connection)
            pos = Coord2D(0, 0)
            dir = Coord2D(1, -1)
            intersect = Coord2D(10, -10)
            // then
            map.get.farthestPosition(pos, dir, Some(quad1)) must be(Some(intersect))

            // given (some weird intersection with connection, walk to corner)
            pos = Coord2D(0, -10)
            dir = Coord2D(5, -10)
            intersect = Coord2D(5, -20)
            // then
            map.get.farthestPosition(pos, dir, Some(quad1)) must be(Some(intersect))
            map.get.farthestPosition(pos, dir, Some(quad2)) must be(Some(intersect))

            // given (some weird intersection with connection, walk in connection direction)
            pos = Coord2D(0, -10)
            dir = Coord2D(1, 0)
            intersect = Coord2D(10, -10)
            // then
            map.get.farthestPosition(pos, dir, Some(quad1)) must be(Some(intersect))
            map.get.farthestPosition(pos, dir, Some(quad2)) must be(Some(intersect))

            // given (more special cases)
            pos = Coord2D(10, -10)
            dir = Coord2D(-1, 0)
            intersect = Coord2D(-10, -10)
            // then
            map.get.farthestPosition(pos, dir, Some(quad1)) must be(Some(intersect))
            map.get.farthestPosition(pos, dir, Some(quad2)) must be(Some(intersect))

            // given (more special cases, walking against the wall)
            pos = Coord2D(10, -10)
            dir = Coord2D(0, -1)
            intersect = Coord2D(10, -10)
            // then
            map.get.farthestPosition(pos, dir, Some(quad1)) must be(Some(intersect))
            map.get.farthestPosition(pos, dir, Some(quad2)) must be(Some(intersect))

            // given
            pos = Coord2D(2, 15) // outside T1, north-east
            dir = Coord2D(0, -1) // pointing directly south
            intersect = Coord2D(2, 10)
            // then
            map.get.farthestPosition(pos, dir) must be(None)
            map.get.farthestPosition(pos, dir, Some(quad1)) must be(None)
        }
    }
}


/**
 * Layout of the sample pathing map:
 *
 *                    N
 *
 *                    ^
 *          -5        |        5
 *            --------|--------
 *           /        |10      \
 *          /         |         \
 *         /          |          \
 *        /           |           \   x-axis
 *--------------------+--------------------> E
 *      /             |             \
 *     /              |              \
 *    /         T1    |               \
 *   /-10             |-10          10 \
 *  ------------------|------------------
 *   \_               |               _/
 *     \_       T2    |             _/
 *       \_           |           _/
 *         \_         |-20      _/
 *           ---------|---------
 *         -5         |         5
 *                    | y-axis
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
                "y":10
            },
            "south":
            {
                "west":-10,
                "east":10,
                "y":-10
            },
            "west":
            {
                "north":{"x":-5,"y":10},
                "south":{"x":-10,"y":-10}
            },
            "east":
            {
                "north":{"x":5,"y":10},
                "south":{"x":10,"y":-10}
            }
        },
        {
            "id":1,
            "north":
            {
                "west":-10,
                "east":10,
                "y":-10
            },
            "south":
            {
                "west":-5,
                "east":5,
                "y":-20
            },
            "west":
            {
                "north":{"x":-10,"y":-10},
                "south":{"x":-5,"y":-20}
            },
            "east":
            {
                "north":{"x":10,"y":-10},
                "south":{"x":5,"y":-20}
            }
        }
    ],
    "connections":
    [
        {
            "id":0,
            "west":-10,
            "east":10,
            "y":-10,
            "north":0,
            "south":1
        }
    ]
}"""
}