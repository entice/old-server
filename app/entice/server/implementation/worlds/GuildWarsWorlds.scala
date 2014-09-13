/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.worlds

import entice.server._


// Defines all (serverwise-)available guild wars maps
trait HeroesAscent extends DefaultWorld { self: Core with Tracker =>
  lazy val heroesAscent = WorldImpl("HeroesAscent", self.tracker)
  abstract override def worldStack = super.worldStack ::: heroesAscent :: Nil
}

trait TeamArenas extends DefaultWorld { self: Core with Tracker =>
  lazy val teamArenas = WorldImpl("TeamArenas", self.tracker)
  abstract override def worldStack = super.worldStack ::: teamArenas :: Nil
}

trait RandomArenas extends DefaultWorld { self: Core with Tracker =>
  lazy val randomArenas = WorldImpl("RandomArenas", self.tracker)
  abstract override def worldStack = super.worldStack ::: randomArenas :: Nil
}
