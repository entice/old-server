/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.loggers

import entice.server._

import org.slf4j.{ Logger => SLF4JLogger, LoggerFactory }


/**
 * Simple logging for scala :)
 */
trait DefaultLogger extends Logger {
  lazy val loggerName = "Cake"
  lazy val logger: SLF4JLogger = LoggerFactory.getLogger(loggerName)
}
