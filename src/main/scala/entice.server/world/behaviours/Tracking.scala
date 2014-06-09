/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world
package behaviours

import entice.server.Named
import entice.server.events._

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.Future


case object TrackingFactory extends BehaviourFactory[Tracking] {
  val requires = has[Vision] :: Nil
  val creates  = Tracking
}

/** Actor based tracking */
case class Tracking(override val entity: Entity) extends Behaviour(entity) {
  implicit val actor   = actorSystem.actorOf(Props(new TrackingActor()))
  override val handles = 
    incoming[EntityAdd] ::
    incoming[EntityRemove] ::
    incoming[AttributeAdd] ::
    incoming[AttributeRemove] ::
    incoming[AttributeChange] ::
    Nil

  def track(upd: Update) = entity.world.tracker.trackMe(entity, upd)

  def canSee(e: Entity) = entity.get[Vision] match {
    case Some(v) => v.sees().contains(e)
    case _       => false 
  }

  class TrackingActor extends Actor with ActorLogging {
    def receive = {
      case Evt(u: Update) if (u.entity == entity) => track(u)
      case Evt(u: Update) if (canSee(u.entity))   => track(u)
    }
  }
}