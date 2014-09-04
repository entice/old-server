/**
 * For copyright information see the LICENSE document.
 */

package entice.server


package object macros {
  import scala.language.experimental.{ macros => scalaMacros }
  import scala.reflect.macros.blackbox.Context

  /** Macro implementation for getting the type-name of any type. */
  implicit def materializeNamed[T]: Named[T] = macro materializeNamedImpl[T]

  def materializeNamedImpl[T: c.WeakTypeTag](c: Context): c.Expr[Named[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val tpeName = tpe.toString()

    c.Expr[Named[T]] { q"""
      new Named[$tpe] {
        val name: String = $tpeName
      }
    """ }
  }
}
