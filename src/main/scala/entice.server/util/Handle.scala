/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package util

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
  var allIds: Set[Int] = Set()
  val random = new Random()
  
  def getNew(): Int = {
    val num = random.nextInt()
    if (allIds.contains(num)) { getNew() }
    else { allIds = allIds + num; num }
  }

  def freeId(id: Int) { allIds = allIds - id }
}