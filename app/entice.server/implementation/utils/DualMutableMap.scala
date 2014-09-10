/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.utils


case class DualMutableMap[T, K]() {
  var tkMap: Map[T, K] = Map()
  var ktMap: Map[K, T] = Map()

  def >>(t: T) : Option[K] = tkMap.get(t)
  def <<(k: K) : Option[T] = ktMap.get(k)
  def valuesLeft           = ktMap.values.toList
  def valuesRight          = tkMap.values.toList

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
