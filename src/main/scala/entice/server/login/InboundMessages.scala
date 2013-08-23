/**
 * For copyright information see the LICENSE document.
 */

package entice.server.login

import entice.server
import entice.protocol.LoginRequest


case class LoginRequestMsg(msg: LoginRequest) extends SessionMixin