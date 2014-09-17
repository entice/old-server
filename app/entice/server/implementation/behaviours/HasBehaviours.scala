/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.behaviours

import entice.server.macros._
import entice.server.implementation.utils.ReactiveTypeMap


trait HasBehaviours {
  def behav: ReactiveTypeMap[Behaviour]

  def hasBehaviour   [T <: Behaviour : Named]: Boolean = behav.contains[T]
  def removeBehaviour[T <: Behaviour : Named]: this.type = { behav.remove[T]; this }
  def addBehaviour   [T <: Behaviour : Named](c: T): this.type = { behav.set(c); this }
}
