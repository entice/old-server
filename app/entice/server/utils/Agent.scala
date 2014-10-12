/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import scala.concurrent.{ExecutionContext, Future}


case class Agent[T](initial: T)(implicit ctx: ExecutionContext) {
  var internal = Future.successful[T](initial)

  def get() = apply()
  def apply(): Future[T] = alter { x => x }
  def alter(f: (T => T)): Future[T] = {
    internal = internal.map(f)
    internal
  }
}
