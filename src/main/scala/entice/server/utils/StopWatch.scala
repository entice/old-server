/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils


/**
 * This is a trait because we need to replace the time measurement in the test
 * implementations - so we can determine the exact result.
 */
trait StopWatch {
    def reset()
    def current : Long
}

/**
 * Serves as a universial time measurement utility
 */
case class SystemStopWatch(/*TODO: include unit*/) extends StopWatch {

    var timeSnapshot = System.currentTimeMillis()

    def reset   { timeSnapshot = System.currentTimeMillis() }
    def current = System.currentTimeMillis() - timeSnapshot
}