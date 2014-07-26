/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world
package attributes

import entice.protocol._


/** The appearance of a player */
case class Appearance(
    profession: Int = 1,
    campaign: Int = 0,
    sex: Int = 1,
    height: Int = 0,
    skinColor: Int = 3,
    hairColor: Int = 0,
    hairstyle: Int = 7,
    face: Int = 31) extends Attribute