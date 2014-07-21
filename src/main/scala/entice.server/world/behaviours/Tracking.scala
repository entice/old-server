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

  /** Watches for tracking events of this entity, or of entities that this can see */
  class TrackingActor extends Actor with ActorLogging {
    import context.dispatcher

    def receive = {
      case Evt(add: EntityAdd) => track(add) // always track
      case Evt(rem: EntityRemove) => track(rem) // always track
      case Evt(u: Update) if (u.entity == entity) => track(u)
      case Evt(u: Update) => canSee(u.entity) onSuccess { case c => if (c) track(u) }
    }

    def canSee(e: Entity): Future[Boolean] = entity.get[Vision] match {
      case Some(vision) => vision.map { v => v.sees.contains(e) }
      case _            => Future.successful(false)
    }
  }
}
