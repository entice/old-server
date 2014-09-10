/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.attributes

import entice.server.macros._
import entice.server.implementation.utils._

import scala.concurrent.Future


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
