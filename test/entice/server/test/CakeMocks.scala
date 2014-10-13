/**
 * For copyright information see the LICENSE document.
 */

package entice.server.test

import entice.server._
import entice.server.handles._
import entice.server.utils._
import org.specs2.mock._


trait CakeMocks extends Mockito {

  /** Do nothing, no worlds */
  trait MockWorlds extends Worlds { self: Entities =>
    def World(name: String, eventBus: EventBus) = smartMock[WorldLike].asInstanceOf[World]
  }
}
