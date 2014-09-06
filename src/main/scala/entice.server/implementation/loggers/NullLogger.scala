/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.loggers

import entice.server._

import org.slf4j.{ Logger => SLF4JLogger, LoggerFactory }


/**
 * Logging to /dev/null
 */
trait NullLogger extends Logger {
  lazy val logger: SLF4JLogger = LoggerFactory.getLogger(classOf[NullLogger])
}
