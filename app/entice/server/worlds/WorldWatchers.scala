/**
 * For copyright information see the LICENSE document.
 */

package entice.server.worlds

import akka.actor.{Actor, ActorRef, Props}
import entice.server._, macros._
import entice.server.handles.{Clients, Entities}
import entice.server.utils.{EventBus, Evt}
import play.api._


trait WorldWatchers extends Worlds {
    self: Core
      with Tracker
      with Entities
      with Clients
      with Attributes
      with Behaviours
      with WorldEvents =>

  import clients.{Idle, LoadingMap}

  trait WorldWatcher extends WorldLike { self: World =>

    private val entityWatcher: ActorRef = actorSystem.actorOf(Props(EntityWatcher(eventBus, behaviours)))
    private val sessionWatcher: ActorRef = actorSystem.actorOf(Props(SessionWatcher(eventBus, self)))

    /** General purpose world-event watcher for the entities of this world */
    case class EntityWatcher(eventBus: EventBus, behaviours: List[BehaviourFactory[_]]) extends Actor {
      eventBus.sub[AttributeAdd]
      eventBus.sub[AttributeRemove]

      def receive = {
        case Evt(_, AttributeAdd(entity, _)) => behaviours.foreach { _.applyTo(entity) }
        case Evt(_, AttributeRemove(entity, _)) => behaviours.foreach { _.removeFrom(entity) }
      }
    }

    /** General purpose world-event watcher for the entities of this world */
    case class SessionWatcher(eventBus: EventBus, world: Worlds#World) extends Actor {
      eventBus.sub[PlayerJoin]
      eventBus.sub[PlayerQuit]

      def receive = {
        case Evt(sender, PlayerJoin(client, chara)) if (sender.isDefined) =>
          Logger.info(s"[${world.name}] Player connected: ${client().account.email} with '${chara}'")
          client.update(client().copy(state = LoadingMap(sender.get, world: Worlds#World, chara)))
          // TODO world load

        case Evt(_, PlayerQuit(client)) =>
          Logger.info(s"[${world.name}] Player disconnected: ${client().account.email} with '${client().state.chara}'")
          client.update(client().copy(state = Idle()))

        case e => Logger.debug(s"Session watcher received unknown event: ${e}")
      }
    }
  }
}



