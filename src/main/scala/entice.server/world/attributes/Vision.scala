/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world
package attributes

import entice.protocol._


/** List of entities that this entity can see if any */
case class Vision(sees: Set[Entity] = Set()) extends Attribute with NoPropagation