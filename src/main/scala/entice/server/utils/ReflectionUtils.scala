/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import shapeless._
import scala.reflect.runtime.universe._


/**
 * Encapsulates functionality we need to perform certain type checks in the world.
 */
object ReflectionUtils {

    import HList.ListCompat._

    /**
     * Extract the reflection api Type of a scala object's type
     */
    def getType[T : TypeTag](t: T) = typeOf[T]


    /**
     * Check if a hlist of types contains a certain type
     */
    def contains(list: Type, t: Type) = {
        def rec(l: Type, acc: List[Type]): List[Type] = {
            l match {
                case TypeRef(_, _, in :: tail :: Nil) if t <:< in =>
                    rec(tail, if (acc contains in) acc else in :: acc)
                case TypeRef(_, _, _ :: tail :: Nil) =>
                    rec(tail, acc)
                case _ => acc.reverse
            }
        }
        val n = typeOf[Nothing]
        ! (if (t =:= n) List(n) else rec(list, Nil)).isEmpty
    }


    /**
     * Check if a hlist of types contains all types given by another hlist
     */
    def containsAll(l1: Type, l2: Type) = {
        def rec(l: Type, acc: List[Type]): List[Type] = {
            l match {
                case TypeRef(_, _, in :: tail :: Nil) =>
                    rec(tail, if (contains(l1, in)) acc else in :: acc)
                case _ => acc.reverse
            }
        }
        val n = typeOf[Nothing]
        (if (l2 =:= n) List(n) else rec(l2, Nil)).isEmpty
    }


    /**
     * Check if a case class's properties hlist contains a property of a certain type
     */
    def ccontains[T, R : TypeTag](t: T, tp: Type)(implicit gen: Generic.Aux[T, R]) = {
        contains(getType(gen.to(t)), tp)
    }


    /**
     * Check if a case class's properties hlist contains properties of all given types
     */
    def ccontainsAll[T, R : TypeTag](t: T, tp: Type)(implicit gen: Generic.Aux[T, R]) = {
        containsAll(getType(gen.to(t)), tp)
    }


    /**
     * Check if a given hlist (instance) conains a certain type
     */
    def hcontains[T <: HList : TypeTag](t: T, tp: Type) = {
        contains(getType(t), tp)
    }


    /**
     * Check if a given hlist (instance) contains all given types
     */
    def hcontainsAll[T <: HList : TypeTag](t: T, tp: Type) = {
        containsAll(getType(t), tp)
    }


    /**
     * Convert a HList type to a list of types
     */
    def htoTypes(t: Type) = {
        def rec(l: Type, acc: List[Type]): List[Type] = {
            l match {
                case TypeRef(_, _, in :: tail :: Nil) =>
                    rec(tail, in :: acc)
                case _ => acc.reverse
            }
        }
        rec(t, Nil)
    }
}