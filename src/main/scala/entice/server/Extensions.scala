/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.utils._
import entice.protocol._
import akka.actor.{ Actor, ActorRef, ActorSystem }
import akka.actor.{ Extension, ExtensionId, ExtensionIdProvider, ExtendedActorSystem }


/**
 * Adds the configuration to your actor.
 */
trait Subscriber { self: Actor =>
    def messageBus = MessageBusExtension(context.system)
    def subscriptions: List[Class[_ <: Typeable]]
    def publish(msg: Typeable) = messageBus.publish(MessageEvent(this.self, msg))
    def publish(sender: ActorRef, msg: Typeable) = messageBus.publish(MessageEvent(sender, msg))
    def register() { 
        subscriptions foreach { messageBus.subscribe(this.self, _) }
    }
}

object MessageBusExtension 
    extends ExtensionId[MessageBus]
    with ExtensionIdProvider {

    override def lookup = MessageBusExtension
    override def createExtension(system: ExtendedActorSystem) = new MessageBus
    override def get(system: ActorSystem): MessageBus = super.get(system)
}


/**
 * Add the client registry to your actor.
 */
trait Clients { self: Actor =>
    def clients = ClientRegistryExtension(context.system)
}

object ClientRegistryExtension 
    extends ExtensionId[ClientRegistry]
    with ExtensionIdProvider {

    override def lookup = ClientRegistryExtension
    override def createExtension(system: ExtendedActorSystem) = new ClientRegistry
    override def get(system: ActorSystem): ClientRegistry = super.get(system)
}


/**
 * Add the client registry to your actor.
 */
trait Worlds { self: Actor =>
    def worlds = WorldRegistryExtension(context.system)
}

object WorldRegistryExtension 
    extends ExtensionId[WorldRegistry]
    with ExtensionIdProvider {

    override def lookup = WorldRegistryExtension
    override def createExtension(system: ExtendedActorSystem) = new WorldRegistry
    override def get(system: ActorSystem): WorldRegistry = super.get(system)
}


/**
 * Adds the configuration to your actor.
 */
trait Configurable { self: Actor =>
    def config = ConfigExtension(context.system)
}

object ConfigExtension 
    extends ExtensionId[Config]
    with ExtensionIdProvider {

    override def lookup = ConfigExtension
    override def createExtension(system: ExtendedActorSystem) = Config.fromFile ("config/config.json") getOrElse (Config.default)
    override def get(system: ActorSystem): Config = super.get(system)
}



