/**
 * For copyright information see the LICENSE document.
 */

package entice.server.worlds

import entice.server._
import entice.server.enums._
import entice.server.handles._
import entice.server.utils.EventBus


trait GuildWarsWorlds extends Worlds
    with WorldBase
    with WorldTracking
    with WorldWatchers {
  self: Core
    with Tracker
    with Entities
    with Clients
    with Behaviours =>

  type World = GuildWarsWorld
  def World(name: String, map: WorldMap.WorldMapVal): World = new GuildWarsWorld(name, map)

  class GuildWarsWorld(val name: String, val map: WorldMap.WorldMapVal)
    extends WorldLike
    with WorldImpl
    with WorldTracker
    with WorldWatcher

  // All accessible worlds
  lazy val heroesAscent = World("HeroesAscent", WorldMap.HeroesAscent)
  lazy val teamArenas   = World("TeamArenas",   WorldMap.TeamArenas)
  lazy val randomArenas = World("RandomArenas", WorldMap.RandomArenas)

  abstract override def allWorlds = super.allWorlds ::: heroesAscent :: teamArenas :: randomArenas :: Nil
}
