/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import scala.collection.mutable._
import scala.reflect.runtime.universe._


case class DualMutableMap[T, K] (
        var tkMap: Map[T, K] = BlockingMutableMap[T, K](), 
        var ktMap: Map[K, T] = BlockingMutableMap[K, T]()) {

    def >>(t: T) : Option[K] = tkMap.get(t)
    def <<(k: K) : Option[T] = ktMap.get(k)
    def valuesLeft           = ktMap.values
    def valuesRight          = tkMap.values

    def +=(n: (T, K)) {
        tkMap += (n._1 -> n._2)
        ktMap += (n._2 -> n._1)
    }

    def removeLeft(t: T) {
        tkMap.get(t) map ktMap.remove
        tkMap -= t
    }

    def removeRight(k: K) {
        ktMap.get(k) map tkMap.remove
        ktMap -= k
    }
}