/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.loggers

import entice.server._

import org.slf4j.{ Logger => SLF4JLogger, LoggerFactory }


/**
 * Logging to stdout only
 */
trait TestLogger extends Logger {
  lazy val logger: SLF4JLogger = LoggerFactory.getLogger(classOf[TestLogger])
}
