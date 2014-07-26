/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world
package attributes

import entice.protocol._


/** Present if this entity can perform animations */
case class Animation(id: Animations.AniVal = Animations.None) extends Attribute