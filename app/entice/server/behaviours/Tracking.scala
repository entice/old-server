/**
 * For copyright information see the LICENSE document.
 */

package entice.server.behaviours

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import entice.server._, macros._
import entice.server.handles.Entities
import entice.server.utils.Evt

import scala.concurrent.Future
import scala.language.postfixOps


/** Tracks entity events for this and other entities */
trait Tracking extends Behaviours {
  self: Core
    with Tracker
    with Worlds
    with Entities
    with Attributes
    with WorldEvents =>

  import entities.EntityHandle

  abstract override def behaviours: List[BehaviourFactory[_]] =
    super.behaviours :::
    TrackingFactory ::
    Nil

  object TrackingFactory extends BehaviourFactory[Tracking]() {
    val requires = has[Vision] :: Nil
    val creates  = Tracking
  }

  /** Actor based tracking */
  case class Tracking(override val entity: EntityHandle) extends Behaviour(entity) {
    implicit val actor: ActorRef = actorSystem.actorOf(Props(new TrackingActor()))
    override val handles =
      incoming[EntityAdd] ::
      incoming[EntityRemove] ::
      incoming[AttributeAdd] ::
      incoming[AttributeRemove] ::
      incoming[AttributeChange] ::
      Nil

    def track(upd: Update) = tracker.trackMe(entity, upd)

    /** Watches for tracking events of this entity, or of entities that this can see */
    class TrackingActor extends Actor with ActorLogging {
      import context.dispatcher

      def receive = {
        case Evt(_, add: EntityAdd) => track(add) // always track
        case Evt(_, rem: EntityRemove) => track(rem) // always track
        case Evt(_, u: Update) if (u.entity == entity) => track(u)
        case Evt(_, u: Update) => canSee(u.entity) onSuccess { case c => if (c) track(u) }
      }

      def canSee(e: EntityHandle): Future[Boolean] = entity().get[Vision] match {
        case Some(vision) => vision.map { v => v.sees.contains(e) }
        case _            => Future.successful(false)
      }
    }
  }
}
