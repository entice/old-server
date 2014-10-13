/**
 * For copyright information see the LICENSE document.
 */

package entice.server.worlds

import entice.server._
import entice.server.handles.{Clients, Entities}
import entice.server.utils.EventBus


trait GuildWarsWorlds extends Worlds with WorldBase with WorldTracking with WorldWatchers {
    self: Core
      with Tracker
      with Entities
      with Clients
      with Behaviours
      with WorldEvents =>

  def World(name: String, eventBus: EventBus = new EventBus()): World = new World(name, eventBus)

 class World(val name: String, val eventBus: EventBus = new EventBus())
    extends WorldLike
    with WorldImpl
    with WorldTracker
    with WorldWatcher

  // All accessible worlds
  lazy val heroesAscent = World("HeroesAscent")
  lazy val teamArenas   = World("TeamArenas")
  lazy val randomArenas = World("RandomArenas")

  abstract override def allWorlds = super.allWorlds ::: heroesAscent :: teamArenas :: randomArenas :: Nil
}
