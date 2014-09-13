/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.events

import akka.actor.ActorRef


sealed trait NetEvent
case class NewSession(session: ActorRef)  extends NetEvent // from eventBus
case class LostSession(session: ActorRef) extends NetEvent // from eventBus
case class KickSession()                  extends NetEvent // to session actor
