/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world
package attributes

import entice.protocol._


/** The state of a group entity */
case class GroupState(
    members: List[Entity] = Nil,
    invited: List[Entity] = Nil,
    joinRequests: List[Entity] = Nil) extends Attribute