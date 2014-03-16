/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.physics

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
                    map.get.quadliteralFor(newSpawn) must not be(None)
                }
                info(value.toString + " seems to work.")
            }
        }

        // "plot a map" in {
        //     val mapData = Maps.withMapName("HeroesAscent")
        //     val map = PathingMap.fromFile(defaultDir + mapData.pmap)

        //     map must not be(None)
        //     map.get.safeWithSpawnAs("HeroesAscent_test_1.svg", Coord2D(2372.9268F,-3225.669F))
        //     map.get.safeWithSpawnAs("HeroesAscent_test_2.svg", Coord2D(2392.5117F,-3210.0413F))
        // }
    }
}