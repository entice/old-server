/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils


/**
 * Serves as a universial time mesurement utility
 */
case class StopWatch(/*TODO: include unit*/) {

    var timeSnapshot = System.currentTimeMillis()

    def reset   { timeSnapshot = System.currentTimeMillis() }
    def current = System.currentTimeMillis() - timeSnapshot
}