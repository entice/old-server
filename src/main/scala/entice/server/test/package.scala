/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.utils._
import entice.protocol._
import akka.actor._


package object test {

    /**
     * Bypasses a messagebus call by a sending messagevent to an actor directly.
     */
    def fakePub(recipient: ActorRef, sender: ActorRef, msg: Typeable) { 
        recipient ! MessageEvent(sender, msg)
    }
}