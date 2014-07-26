/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import Named._
import util.{ Agent, Handle, ReactiveTypeMap }
import events._

import akka.actor.{ ActorRef, ActorSystem }
import scala.concurrent.{ Future, ExecutionContext }


/** General entity contract. Not tracking involved at this level */
trait Entity 
    extends Handle 
    with HasAttributes 
    with HasBehaviours {
  def world: World
}


/** General entity implementation. Pushes out tracking events. */
case class EntityImpl private[world] (
    world: World, 
    initialAttr: Option[ReactiveTypeMap[Attribute]] = None) 
    extends Entity  
    with EntityTracker {

  import world.actorSystem.dispatcher
  val ctx = world.actorSystem.dispatcher
  // create attribute and behaviour maps
  val attr = initialAttr.getOrElse(new ReactiveTypeMap())
  val behav = new ReactiveTypeMap[Behaviour]()
}


/** Add automagical tracking to an entities 'state changes'. Needs Tracking behviour */
trait EntityTracker extends HasAttributes { self: Entity with HasAttributes =>

  implicit def ctx: ExecutionContext 

  def track[T <: Update : Named](update: T) = { 
    implicit val actor: ActorRef = null 
    world.eventBus.pub(update) 
  }
  
  abstract override def add[T <: Attribute : Named](c: T) = {
    super.get[T] match {
      case Some(attr) => attr onSuccess { case a if (a != c) => track(AttributeChange(self, a, c)) }
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