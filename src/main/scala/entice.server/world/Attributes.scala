/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import util.ReactiveTypeMap

import entice.protocol._

import scala.concurrent.Future
import scala.collection._


trait Attribute extends mutable.Cloneable[Attribute] with TrackingOptions


trait NoPropagation extends TrackingOptions { override def notPropagated = true }
trait NoVisibility  extends TrackingOptions { override def notVisible = true }


trait HasAttributes {
  def attr: ReactiveTypeMap[Attribute]

  def has[T <: Attribute : Named]: Boolean = attr.contains[T]

  /** Unsafe get. Returns the future value if possible. */
  def apply[T <: Attribute : Named]: Future[T] = attr.get[T].get

  /** Optional get. Returns an option of the future value. Always. */
  def get[T <: Attribute : Named]: Option[Future[T]] = attr.get[T]
  
  /** Remove an attribute from the set. */
  def -     [T <: Attribute : Named]: this.type = remove[T]
  def remove[T <: Attribute : Named]: this.type = { attr.remove[T]; this }

  /** Add or set an attribute. */
  def set   [T <: Attribute : Named](c: T): this.type = add(c)
  def +     [T <: Attribute : Named](c: T): this.type = add(c)
  def add   [T <: Attribute : Named](c: T): this.type = { attr.set(c); this }
}