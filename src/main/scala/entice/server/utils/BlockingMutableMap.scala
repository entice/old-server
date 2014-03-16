/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import scala.collection.mutable.{ Map, SynchronizedMap, HashMap }


object BlockingMutableMap {
    def apply[T, K](): Map[T, K] = {
        new HashMap[T, K] with SynchronizedMap[T, K]
    }
}