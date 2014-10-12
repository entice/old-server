/**
 * For copyright information see the LICENSE document.
 */

package entice.server.enums

import entice.server.utils.Coord2D
import play.api.libs.json.Format


/**
 * All accessible maps...
 */
object WorldMap extends Enumeration {

  type WorldMap = Value
  // internal structure
  class WorldMapVal(name: String, val pmap: String, val groupSize: Int, val spawns: List[Coord2D]) extends Val(nextId, name)
  protected final def Value(name: String, pmap: String, groupSize: Int, spawns: List[Coord2D]): WorldMapVal = new WorldMapVal(name, pmap, groupSize, spawns)

  // workaround for withName
  final def withMapName(name: String): WorldMapVal = super.withName(name).asInstanceOf[WorldMapVal]

  val HeroesAscent            = Value("HeroesAscent",     "heroes_ascent.json",   8, List(Coord2D(2017, -3241)))
  val TeamArenas              = Value("TeamArenas",       "team_arenas.json",     4, List(Coord2D(-1873, 352)))
  val RandomArenas            = Value("RandomArenas",     "random_arenas.json",   1, List(Coord2D(3854, 3874)))
  implicit def enumFormat: Format[WorldMap] = EnumUtils.enumFormat(WorldMap)
}
