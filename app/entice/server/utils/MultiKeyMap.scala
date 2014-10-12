/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils


/**
 * This map can use multiple keys.
 * (they all live in the same hash space, so be careful)
 * Usage:
 * @tparam K    This is a tuple of accepted key types.
 * @tparam V    This is the type of the value.
 */
case class MultiKeyMap[K <: Product, V]() {
  var inner: Map[Any, (K, V)] = Map()

  def contains(key: Any) = inner.contains(key)

  def +(keys: K, value: V) {
    val it = keys.productIterator
    for (key <- it) {
      inner = inner + (key -> (keys, value))
    }
  }

  def -(key: Any) {
    if (!contains(key)) { return }
    val it = inner(key)._1.productIterator
    for (key <- it) {
      inner = inner - key
    }
  }

  def apply(key: Any): V       = inner.get(key).get._2
  def get(key: Any): Option[V] = inner.get(key).map { x => x._2 }
}