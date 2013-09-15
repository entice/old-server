/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.server.utils._
import entice.protocol._

import akka.actor.ActorRef

import scala.util._


/**
 * Client data storage
 * TODO: add DAO stuff
 */
case class Client(
    uuid: UUID = UUID(), 
    session: ActorRef,
    gsKey: Long = new Random() nextLong)