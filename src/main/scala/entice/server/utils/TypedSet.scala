/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import scala.reflect.runtime.universe._


/**
 * Keeps one entry for each subtype of type E
 */
class TypedSet[E : TypeTag] private (var map: Map[Type, E] = Map()) {
    def this    ()                           = this(Map())
    def +       [T <: E : TypeTag](value: T) = new TypedSet[E](map + (typeOf[T] -> value))
    def remove  [T <: E : TypeTag]           = new TypedSet[E](map - typeOf[T])
    def apply   [T <: E : TypeTag]           = this.get[T].get
    def get     [T <: E : TypeTag]           = map.get(typeOf[T]).asInstanceOf[Option[T]]
    def contains[T <: E : TypeTag]           = map.contains(typeOf[T])
    def contains(t: Type)                    = map.contains(t)
    def toList                               = map.values.toList.asInstanceOf[List[E]]
}