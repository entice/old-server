/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world
package behaviours

import entice.server.Named
import entice.server.events._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.Future


case object TrackingFactory extends BehaviourFactory[Tracking] {
  val requires = has[Vision] :: Nil
  val createInternal = Tracking
}

/** Actor based tracking */
case class Tracking(override val entity: Entity) extends Behaviour(entity) {
  val actor = actorSystem.actorOf(Props(new TrackableActor()))

  def track(upd: Update) = entity.world.tracker.trackMe(entity, upd)

  def canSee(e: Entity) = entity.get[Vision] match {
    case Some(v) => v.sees().contains(e)
    case _       => false 
  }

  class TrackableActor extends Actor {
    def receive = {
      case Evt(u: Update) if (u.entity == entity) => track(u)
      case Evt(u: Update) if (canSee(u.entity))   => track(u)
    }
  }
}