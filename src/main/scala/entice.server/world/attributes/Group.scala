/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world
package attributes

import entice.protocol._


/** Present if this entity can be part of a group */
case class Group(group: Entity) extends Attribute with NoPropagation