/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import Named._


trait TakesComponents {
  def takes(e: Entity) = requires forall { _.check(e) }

  def requires: List[Requirement] 

  sealed trait Requirement { def check(e: Entity): Boolean }

  /** Defines a type that MUST be present in the entity */
  def        has[T <: Component : Named] = Has[T]()
  case class Has[T <: Component : Named]() extends Requirement { 
    def check(e: Entity): Boolean = (e.has[T])
  }

  /** Defines a type that MUST NOT be present in the entity */
  def        hasNot[T <: Component : Named] = HasNot[T]()
  case class HasNot[T <: Component : Named]() extends Requirement { 
    def check(e: Entity): Boolean = (!e.has[T])
  }
}