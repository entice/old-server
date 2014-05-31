/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import Named._
import util.NamedTypeMap
import events._


trait EntityHandle {
  def id: Int
}

class Entity(val world: World) extends Trackable {
  implicit val self = this

  var comps = new NamedTypeMap[Component]()

  def get  [T <: Component : Named] = comps.get[T]
  def apply[T <: Component : Named] = comps[T]
  def has  [T <: Component : Named] = comps.contains[T]


  /** Add or change a component, track accordingly */
  def +[T <: Component : Named](c: T) = add(c)
  def add[T <: Component : Named](c: T) = {
    comps.get[T] match {
      case Some(comp) if (c != comp) => comps = comps + c; track(ComponentChange(this, comp, c))
      case None                      => comps = comps + c; track(ComponentAdd(this, c))
      case _                         => // do nothing if component the same
    }
    this
  }
  

  /** Remove a component, track accordingly */
  def -[T <: Component : Named] = remove[T]
  def remove[T <: Component : Named] = {
    comps.get[T] match {
      case Some(c) => comps = comps - c; track(ComponentRemove(this, c))
      case _       => // do nothing if component doesnt exist
    }
    this
  }
  

  /** Same as add, since map doesn't care - but different tracking event */
  def set[T <: Component : Named](c: T) = add(c)
}