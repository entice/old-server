/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import Named._
import util.NamedTypeMap
import events._

import akka.actor.{ ActorRef, ActorSystem }
import akka.agent.Agent


trait EntityHandle {
  def id: Int
}

class Entity(val world: World)  {
  import scala.concurrent.ExecutionContext.Implicits.global

  var comps = new NamedTypeMap[Attribute]()

  def get  [T <: Attribute : Named] = comps.get[T]
  def apply[T <: Attribute : Named] = comps[T]
  def has  [T <: Attribute : Named] = comps.contains[T]


  /** Add or change a component, track accordingly */
  def +[T <: Attribute : Named](c: T) = add(c)
  def add[T <: Attribute : Named](c: T) = { comps = comps + c; this }
  

  /** Remove a component, track accordingly */
  def -[T <: Attribute : Named] = remove[T]
  def remove[T <: Attribute : Named] = { comps = comps.remove[T]; this }
  

  /** Same as add, since map doesn't care - but different tracking event */
  def set[T <: Attribute : Named](c: T) = add(c)
}


trait EntityTracker extends Entity { self: Entity =>

  def track(update: Update) = { 
    implicit val actor: ActorRef = null 
    world.eventBus.pub(update) 
  }
  
  abstract override def add[T <: Attribute : Named](c: T) = {
    comps.get[T] match {
      case Some(comp) if (c != comp) => track(AttributeChange(self, comp, c))
      case None                      => track(AttributeAdd(self, c))
      case _                         => // do nothing if component the same
    }
    super.add(c)
  }

  abstract override def remove[T <: Attribute : Named] = {
    comps.get[T] match {
      case Some(c) => track(AttributeRemove(self, c))
      case _       => // do nothing if component doesnt exist
    }
    super.remove[T]
  }
}