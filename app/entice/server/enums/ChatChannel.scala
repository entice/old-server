/**
 * For copyright information see the LICENSE document.
 */

package entice.server.enums

import play.api.libs.json.Format


/**
 * The chat channel for a channel-based chat message
 */
object ChatChannel extends Enumeration {

  type ChatChannel = Value

  val All                     = Value("all")
  val Group                   = Value("group")
  // more is yet to come ;)

  implicit def enumFormat: Format[ChatChannel] = EnumUtils.enumFormat(ChatChannel)
}
