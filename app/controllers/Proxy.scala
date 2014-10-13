/**
 * For copyright information see the LICENSE document.
 */

package controllers

import entice.server.Cake
import play.api.mvc._

/** Controller for the static website crap */
object Proxy extends Controller {

  def authGet() = Cake.authControl.authGet()
  def loginPost() = Cake.authControl.loginPost()
  def logoutPost() = Cake.authControl.logoutPost()

  def webLobbyGet() = Cake.lobbyControl.webLobbyGet()
  def apiLobbyGet() = Cake.lobbyControl.apiLobbyGet()

  def charGet(action: String, name: String) = Cake.charControl.charGet(action, name)
  def charPost(action: String, name: String) = Cake.charControl.charPost(action, name)

  def worldGet(chara: String, map: String) = Cake.worldControl.worldGet(chara, map)

  def clientGet(chara: String, map: String) = Cake.clientControl.clientGet(chara, map)
}
