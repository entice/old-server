/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils


case class DualMap[T, K]() {
  var tkMap: Map[T, K] = Map()
  var ktMap: Map[K, T] = Map()

  def >>(t: T) = getLeft(t)
  def getLeft(t: T): Option[K] = tkMap.get(t)

  def <<(k: K) = getRight(k)
  def getRight(k: K): Option[T] = ktMap.get(k)

  def valuesLeft: List[T] = ktMap.values.toList
  def valuesRight: List[K] = tkMap.values.toList

  def containsLeft(t: T): Boolean = tkMap.contains(t)
  def containsRight(k: K): Boolean = ktMap.contains(k)

  def +=(n: (T, K)) {
    tkMap = tkMap + (n._1 -> n._2)
    ktMap = ktMap + (n._2 -> n._1)
  }

  def removeLeft(t: T) {
    tkMap.get(t) map { k => ktMap = ktMap - k }
    tkMap = tkMap - t
  }

  def removeRight(k: K) {
    ktMap.get(k) map { t => tkMap = tkMap - t }
    ktMap = ktMap - k
  }


}
