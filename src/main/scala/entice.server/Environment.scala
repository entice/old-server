/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import org.slf4j.{ Logger => SLF4JLogger }


/**
 * Determins the environment the server runs in.
 */
trait Environment {

  def environment: EnvironmentLike

  def environmentFromProperty(prop: String): EnvironmentLike = {
    prop match {
      case Development.value => Development
      case Test.value        => Test
      case Production.value  => Production
      case _                 => Production
    }
  }

  sealed trait EnvironmentLike { def value: String }
  case object Development extends EnvironmentLike { val value = "DEV" }
  case object Test        extends EnvironmentLike { val value = "TEST" }
  case object Production  extends EnvironmentLike { val value = "PROD" }
}
