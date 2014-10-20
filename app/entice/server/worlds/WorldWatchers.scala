/**
 * For copyright information see the LICENSE document.
 */

package entice.server.worlds

import akka.actor.{Actor, ActorRef, Props}
import entice.server._
import entice.server.attributes._
import entice.server.events._
import entice.server.handles._
import entice.server.macros._
import entice.server.utils._

import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.util.Random


trait WorldWatchers extends Worlds {
    self: Core
      with Tracker
      with Clients
      with Entities
      with Behaviours =>

  import clients._
  import entities._

  trait WorldWatcher extends WorldLike { world: World =>

    override def onStart() {
      super.onStart()
      val entityWatcher: ActorRef = actorSystem.actorOf(Props(EntityWatcher()))
      val sessionWatcher: ActorRef = actorSystem.actorOf(Props(SessionWatcher()))
    }

    /** General purpose world-event watcher for the entities of this world */
    case class EntityWatcher() extends Actor {
      eventBus.sub[AttributeAdd]
      eventBus.sub[AttributeRemove]

      override def preStart() { Logger.info(s"[${world.name}] Registered EntityWatcher.") }

      def receive = {
        case Evt(_, AttributeAdd(entity, _)) => behaviours.foreach { _.applyTo(entity) }
        case Evt(_, AttributeRemove(entity, _)) => behaviours.foreach { _.removeFrom(entity) }
      }
    }

    /** Handle player join / quit */
    case class SessionWatcher() extends Actor {
      eventBus.sub[PlayerJoin]
      eventBus.sub[PlayerQuit]

      override def preStart() { Logger.info(s"[${world.name}] Registered SessionWatcher.") }

      def receive = {
        case Evt(sender, PlayerJoin(client, chara)) if (sender.isDefined) =>
          Logger.info(s"[${world.name}] Player connected: ${client().account.email} with '${chara}'")
          client.update(client().copy(
            entity = world.createEntity(Some(initAttributes(chara, client().chars(chara)))),
            state = LoadingMap(sender.get, world: Worlds#World, chara)))
          // TODO world load

        case Evt(_, PlayerQuit(client)) =>
          Logger.info(s"[${world.name}] Player disconnected: ${client().account.email} with '${client().state.chara}'")
          client.update(client().copy(state = Idle()))

        case e => Logger.debug(s"Session watcher received unknown event: ${e}")
      }

      def initAttributes(name: String, appear: Appearance) = {
        new ReactiveTypeMap[Attribute]()
          .add(Name(name))
          .add(appear)
          .add(if (world.map.spawns.isEmpty) Position() else Position(Random.shuffle(world.map.spawns).head))
          .add(Movement())
      }
    }
  }
}



