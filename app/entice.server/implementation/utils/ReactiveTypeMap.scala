/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.utils

import entice.server.macros._

import scala.concurrent.{ Future, ExecutionContext }


class ReactiveTypeMap[E] private (var map: Map[String, Agent[E]] = Map())(implicit ctx: ExecutionContext) {
  def this()(implicit ctx: ExecutionContext) = this(Map())

  def contains[T <: E : Named] = map.contains(implicitly[Named[T]].name)
  def remove  [T <: E : Named] = map = map - implicitly[Named[T]].name
  def toList: List[Future[E]]  = map.values.map(_.apply()).toList
  override def toString        = map.toString

  def get[T <: E : Named]: Option[Future[T]] = {
    map
      .get(implicitly[Named[T]].name)
      .map { a => a.apply() }
      .map { _.asInstanceOf[Future[T]] }
  }

  def add[T <: E : Named](v: T): this.type = set(v)
  def set[T <: E : Named](v: T): this.type = {
    map.get(implicitly[Named[T]].name) match {
      case Some(agent) => agent.asInstanceOf[Agent[T]].alter { x => v }
      case None        => map = map + (implicitly[Named[T]].name -> Agent[E](v))
    }
    this
  }
}
