/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import scala.reflect.runtime.universe._
import scala.collection._


/**
 * Keeps one entry for each subtype of type E
 */
class TypedSet[E <: mutable.Cloneable[E] : TypeTag] private (var map: Map[Type, E] = Map()) {
    def this    ()                           = this(Map())
    def add     [T <: E : TypeTag](value: T) = new TypedSet[E](map + (typeOf[T] -> value))
    def +       [T <: E : TypeTag](value: T) = add(value)
    def remove  [T <: E : TypeTag]           = new TypedSet[E](map - typeOf[T])
    def -       [T <: E : TypeTag](value: T) = remove[T]
    def apply   [T <: E : TypeTag]           = this.get[T].get
    def get     [T <: E : TypeTag]           = map.get(typeOf[T]).asInstanceOf[Option[T]]
    def contains[T <: E : TypeTag]           = map.contains(typeOf[T])
    def contains(t: Type)                    = map.contains(t)
    def toList                               = map.values.toList.asInstanceOf[List[E]]
    def deepClone                            = new TypedSet[E]((for ((t: Type, e: E) <- map) yield (t -> e.clone)).toMap)


    /**
     * Will replace older elements of the same type in `other`
     */
    def union   (other: TypedSet[E])         = {
        var resultMap: Map[Type, E] = Map()
            other.map.keySet
                .map    { t => resultMap = resultMap + (t -> other.map(t).clone)}
            this.map.keySet
                .map    { t => resultMap = resultMap + (t -> this.map(t).clone)}

        new TypedSet[E](resultMap)
    }


    def intersect(other: TypedSet[E])         = {
        new TypedSet[E](
            this.map.keySet
                .filter { t => other.map.contains(t) && this.map(t) != other.map(t) }
                .map    { t => (t -> this.map(t).clone) }
                .toMap)
    }


    def diff    (other: TypedSet[E])         = {
        new TypedSet[E](
            this.map.keySet
                .filter { t => !other.map.contains(t) || this.map(t) != other.map(t) }
                .map    { t => (t -> this.map(t).clone) }
                .toMap)
    }


    def diffNew (other: TypedSet[E])         = {
        new TypedSet[E](
            this.map.keySet
                .filter { t => !other.map.contains(t) }
                .map    { t => (t -> this.map(t).clone) }
                .toMap)
    }


    def diffOld (other: TypedSet[E])         = {
        new TypedSet[E](
            other.map.keySet
                .filter { t => !this.map.contains(t) }
                .map    { t => (t -> other.map(t).clone) }
                .toMap)
    }
}