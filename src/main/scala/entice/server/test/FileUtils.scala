/**
 * For copyright information see the LICENSE document.
 */

package entice.server.test

import java.io._
import scala.language.reflectiveCalls

case class File(path: String) {
    import File._

    def write(data: String): Unit = writeToFile(path, data)
    def delete() = (new java.io.File(path)).delete()
}

object File {
    def using[A <: {def close(): Unit}, B](resource: A)(f: A => B): B = {
        try f(resource) finally resource.close()
    }

    def writeToFile(path: String, data: String): Unit = {
        using(new FileWriter(path))(_.write(data))
    }

    def appendToFile(path: String, data: String): Unit = {
      using(new PrintWriter(new FileWriter(path, true)))(_.println(data))
    }
}