/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.configs

import entice.server._

import scala.pickling._, json._

import scala.io.Source
import scala.util.Try


trait StaticConfig extends Config { self: Logger =>

  /** We simply use the static default configuration */
  lazy val config = defaultConfig
}
