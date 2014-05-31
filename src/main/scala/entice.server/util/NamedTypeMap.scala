/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package util

import Named._
import scala.collection._


class NamedTypeMap[E <: mutable.Cloneable[E]] private (var map: Map[String, E] = Map()) {
    def this    ()                     = this(Map())
    def add     [T <: E : Named](v: T) = new NamedTypeMap[E](map + (implicitly[Named[T]].name -> v))
    def +       [T <: E : Named](v: T) = add(v)
    def remove  [T <: E : Named]       = new NamedTypeMap[E](map - implicitly[Named[T]].name)
    def -       [T <: E : Named](v: T) = remove[T]
    def get     [T <: E : Named]       = map.get(implicitly[Named[T]].name).map(_.asInstanceOf[T])
    def apply   [T <: E : Named]       = this.get[T].get
    def contains[T <: E : Named]       = map.contains(implicitly[Named[T]].name)
    def toList      : List[E]          = map.values.toList
    def deepClone   : NamedTypeMap[E]  = new NamedTypeMap[E]((for ((t: String, e: E) <- map) yield (t -> e.clone)).toMap)
}