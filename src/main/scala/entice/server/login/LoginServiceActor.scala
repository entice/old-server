/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.protocol.LoginRequest
import entice.protocol.LoginResponse
import entice.protocol.LoginResponse._

import akka.actor.Actor


class LoginServiceActor extends Actor{

    override def preStart = {
        context.system.eventStream.subscribe(context.self, classOf[LoginRequest])
    }

    def receive = {
        case e@ LoginRequest("test", "test") => sender ! LoginResponse(Some(ErrorCode.INVALID_CREDENTIALS))
        case e: LoginRequest => sender ! LoginResponse()
    }
}