/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import Named._
import util.{ Agent, ReactiveTypeMap }
import events._

import akka.actor.{ ActorRef, ActorSystem }
import scala.concurrent.{ Future, ExecutionContext }


trait EntityHandle {
  def id: Int
}

class Entity(val world: World)  {
  import world.actorSystem.dispatcher

  var comps = new ReactiveTypeMap[Attribute]()

  def has[T <: Attribute : Named] = comps.contains[T]

  /** Unsafe get. Returns the future value if possible. */
  def apply[T <: Attribute : Named]: Future[T] = comps.get[T].get

  /** Optional get. Returns an option of the future value. Always. */
  def get[T <: Attribute : Named]: Option[Future[T]] = comps.get[T]
  
  /** Remove an attribute from the set. */
  def -     [T <: Attribute : Named] = remove[T]
  def remove[T <: Attribute : Named] = { comps.remove[T]; this }

  /** Add or set an attribute. */
  def set   [T <: Attribute : Named](c: T) = add(c)
  def +     [T <: Attribute : Named](c: T) = add(c)
  def add   [T <: Attribute : Named](c: T) =  { comps.set(c); this }
}


/** Add automagical tracking to an entities 'state changes'. Needs Tracking behviour */
trait EntityTracker extends Entity { self: Entity =>
  import world.actorSystem.dispatcher

  def track[T <: Update : Named](update: T) = { 
    implicit val actor: ActorRef = null 
    world.eventBus.pub(update) 
  }
  
  abstract override def add[T <: Attribute : Named](c: T) = {
    super.get[T] match {
      case Some(attr) => attr onSuccess { case a => track(AttributeChange(self, a, c)) }
      case None       => track(AttributeAdd(self, c))
      case _          => // do nothing if component the same
    }
    super.add(c)
  }

  abstract override def remove[T <: Attribute : Named] = {
    super.get[T] match {
      case Some(attr) => attr onSuccess { case a => track(AttributeRemove(self, a)) }
      case _          => // do nothing if component doesnt exist
    }
    super.remove[T]
  }
}