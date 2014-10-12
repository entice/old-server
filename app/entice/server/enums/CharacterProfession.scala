/**
 * For copyright information see the LICENSE document.
 */

package entice.server.enums

import play.api.libs.json.Format


/**
 * All available professions for characters.
 */
object CharacterProfession extends Enumeration {

type CharacterProfession = Value
// internal structure
class ProfVal(name: String, val number: Int) extends Val(nextId, name)
  protected final def Value(name: String, number: Int): ProfVal = new ProfVal(name, number)

  // workaround for withName
  final def withProfessionName(name: String): ProfVal = super.withName(name).asInstanceOf[ProfVal]

  val None         = Value("None",         0)
  val Warrior      = Value("Warrior",      1)
  val Ranger       = Value("Ranger",       2)
  val Monk         = Value("Monk",         3)
  val Necromancer  = Value("Necromancer",  4)
  val Mesmer       = Value("Mesmer",       5)
  val Elementalist = Value("Elementalist", 6)
  val Assassin     = Value("Assassin",     7)
  val Ritualist    = Value("Ritualist",    8)
  val Paragon      = Value("Paragon",      9)
  val Dervish      = Value("Dervish",      10)

  implicit def enumFormat: Format[CharacterProfession] = EnumUtils.enumFormat(CharacterProfession)
}
