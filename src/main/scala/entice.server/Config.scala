/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import akka.event.Logging


/**
 * Use any method you like to fill the config, in the concrete implementations
 */
trait Config { self: Logger =>

  /** The actual config, should have been loaded from somewhere */
  def config: ConfigLike

  /** A fallback config, cautious with this in production mode! */
  protected lazy val defaultConfig: ConfigLike = {
    self.logger.warn("Falling back to default server config!")
    Config(
      tick      = 30,
      minUpdate = 50,
      maxUpdate = 250)
  }

  /**
   * Encapsulates the complete config file.
   * (I'd rather not depend on typesafe's config stuff...)
   */
  trait ConfigLike {
      /** The event interval that invokes the general server systems */
      def tick: Int

      /** The minimum event interval that invokes a game-state push to client */
      def minUpdate: Int

      /** The maximum event interval that invokes a game-state push to client */
      def maxUpdate: Int
  }

  /** Default simple config impl */
  case class Config(
      tick: Int,
      minUpdate: Int,
      maxUpdate: Int) extends ConfigLike
}
