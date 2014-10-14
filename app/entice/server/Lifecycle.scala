/**
 * For copyright information see the LICENSE document.
 */

package entice.server


/** Simple global application config settings */
trait Lifecycle {

  /** Stack me! */
  def onStart() {}
  /** Stack me! */
  def onStop() {}

  object lifecycle {
    def serverStart() = onStart()
    def serverStop() = onStop()
  }
}
