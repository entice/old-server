/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.utils

import scala.util.Random


/** Mixes in a (server instance) unique ID as Int */
trait Handle {
  import Handle._
  lazy val id = getNew()

  def disposeHandle() = freeId(id)

  override def equals(smth: Any) =
    smth match {
      case other: this.type => this.id equals other.id
      case _ => false
    }
}


/** Very simple ID manager */
object Handle {
  type ID = Int

  var allIds: Set[ID] = Set()
  val random = new Random()

  def exists(id: ID) = allIds.contains(id)

  def getNew(): ID = {
    val num = random.nextInt()
    if (allIds.contains(num)) { getNew() }
    else { allIds = allIds + num; num }
  }

  def freeId(id: ID) { allIds = allIds - id }
}
