/**
 * For copyright information see the LICENSE document.
 * Adapted from: https://gist.github.com/DeLongey/3757237
 */

package entice.server.utils

import akka.actor.{ActorRef, Extension}
import akka.event.{ActorEventBus, LookupClassification}
import entice.server.macros._


/**
 * Encapsulates a single event.
 * The EventBus will try to classify them by their classname.
 */
case class Evt[T: Named](sender: Option[ActorRef], message: T) {
  def name: String = implicitly[Named[T]].name
}


/**
 * Event bus to route events to their appropriate handler actors.
 * This is an implementation of the aggregator- or reactor design pattern.
 *
 * Details:
 * When subscribing to an event, you actually subscribe to the classname of it.
 * This is because we can get the name at compile time easily, and then work
 * with it as an identifier at runtime.
 *
 * Usage:
 * This might be used with appropriate message handler actors. The message event carries
 * an additional field just for the purpose of giving the handler some kind of information
 * about the sender. When send from an actor, the sender should be implicitly available.
 */
class EventBus extends ActorEventBus with LookupClassification with Extension {

  type Event = Evt[_]
  type Classifier = String

  protected val mapSize = 10

  protected def classify(event: Event): Classifier = event.name

  def sub[T](implicit named: Named[T], subscriber: Subscriber) {
    super.subscribe(subscriber, named.name)
  }

  def pub[T](event: T)(implicit named: Named[T], sender: Subscriber) {
    super.publish(Evt(Some(sender), event))
  }

  def pubAnon[T: Named](event: T) {
    super.publish(Evt(None, event))
  }

  protected def publish(event: Event, subscriber: Subscriber) {
    subscriber ! event
  }
}
