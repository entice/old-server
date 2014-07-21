/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package test

import akka.actor.Actor


/** Debug helper actor :) */
class PrintlnActor extends Actor {
  def receive = {
    case msg => println(f"Got Message: ${msg}")
  }
}
