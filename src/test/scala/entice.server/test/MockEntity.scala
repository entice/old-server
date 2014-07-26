/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package test

import util._
import world._


case class MockEntity(
    world: World = null, 
    attr: ReactiveTypeMap[Attribute] = null,
    behav: ReactiveTypeMap[Behaviour] = null)
    extends Entity