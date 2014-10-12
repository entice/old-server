/**
 * For copyright information see the LICENSE document.
 */

package entice.server.enums

import play.api.libs.json.Format


/**
 * All official campaigns allowed in the character appearance field
 */
object CharacterCampaign extends Enumeration {

  type CharacterCampaign = Value
  // internal structure
  class CampVal(name: String, val number: Int) extends Val(nextId, name)
  protected final def Value(name: String, number: Int): CampVal = new CampVal(name, number)

  // workaround for withName
  final def withCampaignName(name: String): CampVal = super.withName(name).asInstanceOf[CampVal]

  val Trial            = Value("Trial",            0)
  val Prophecies       = Value("Prophecies",       1)
  val Factions         = Value("Factions",         2)
  val Nightfall        = Value("Nightfall",        3)
  val EyeOfTheNorth    = Value("EyeOfTheNorth",    4)
  val BonusMissionPack = Value("BonusMissionPack", 6)

  implicit def enumFormat: Format[CharacterCampaign] = EnumUtils.enumFormat(CharacterCampaign)
}
