/**
 * For copyright information see the LICENSE document.
 */

package entice.server.test

import entice.server.utils._


/**
 * Serves as a universial time measurement utility
 */
case class TestStopWatch(/*TODO: include unit*/) extends StopWatch {

    var timeTemp : Long = 0

    def set(t: Long) { timeTemp = t }
    def reset        { }
    def current      = timeTemp
}