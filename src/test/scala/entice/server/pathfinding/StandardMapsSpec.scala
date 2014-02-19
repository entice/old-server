/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.pathfinding

import entice.server.test._
import entice.server.utils._
import entice.protocol._

import scala.reflect.runtime.universe._

import org.scalatest._
import org.scalatest.matchers._


class StandardMapsSpec 
    extends WordSpec 
    with MustMatchers
    /*with OneInstancePerTest*/  {

    private val defaultDir = Config.get.pmaps

    "With all available pathing-maps the system" must {

        "load and spawn on all standard maps" in {
            Maps.values foreach { value =>
                val mapData = Maps.withMapName(value.toString)
                val map = PathingMap.fromFile(defaultDir + mapData.pmap)
                var newSpawn = Coord2D(0, 0)

                map must not be(None)
                mapData.spawns.foreach { spawn =>
                    // draw pictures
                    newSpawn = Coord2D(spawn.x, spawn.y)
                    // map.get.safeWithSpawnAs(value.toString + "_1.svg", newSpawn)
                    // newSpawn = Coord2D(-spawn.x, spawn.y)
                    // map.get.safeWithSpawnAs(value.toString + "_2.svg", newSpawn)
                    // newSpawn = Coord2D(spawn.x, -spawn.y)
                    // map.get.safeWithSpawnAs(value.toString + "_3.svg", newSpawn)
                    // newSpawn = Coord2D(-spawn.x, -spawn.y)
                    // map.get.safeWithSpawnAs(value.toString + "_4.svg", newSpawn)

                    // newSpawn = Coord2D(spawn.y, spawn.x)
                    // map.get.safeWithSpawnAs(value.toString + "_5.svg", newSpawn)
                    // newSpawn = Coord2D(-spawn.y, spawn.x)
                    // map.get.safeWithSpawnAs(value.toString + "_6.svg", newSpawn)
                    // newSpawn = Coord2D(spawn.y, -spawn.x)
                    // map.get.safeWithSpawnAs(value.toString + "_7.svg", newSpawn)
                    // newSpawn = Coord2D(-spawn.y, -spawn.x)
                    // map.get.safeWithSpawnAs(value.toString + "_8.svg", newSpawn)
                    // test spawn
                    map.get.trapezoidFor(newSpawn) must not be(None)
                }
                info(value.toString + " seems to work.")
            }
        }
    }
}