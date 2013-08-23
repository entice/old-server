/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.protocol.LoginRequest
import entice.protocol.LoginResponse
import entice.protocol.LoginResponse._

import akka.actor.Actor
import akka.event.EventStream


class LoginServiceActor(evtStream: EventStream) extends Actor{


    override def preStart = {
        evtStream.subscribe(context.self, classOf[LoginRequestMsg])
    }


    def receive = {

        case m@ LoginRequestMsg(LoginRequest("test", "test")) => 
            m.session map { _ ! LoginResponse(Some(ErrorCode.INVALID_CREDENTIALS)) }

        case m: LoginRequestMsg => 
            m.session map { _ ! LoginResponse() }
    }
}