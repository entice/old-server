/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import scala.pickling._
import json._

import scala.io._


object Config {

    // config class(es)
    case class EnticeServer(host: String, loginPort: Int, gamePort: Int)

    // configuration
    val default = EnticeServer("127.0.0.1", 8112, 9112)

    def getFromFile(file: String) = {
        val source = Source.fromFile(file).mkString.trim
        toJSONPickle(source).unpickle[EnticeServer]
    }
}