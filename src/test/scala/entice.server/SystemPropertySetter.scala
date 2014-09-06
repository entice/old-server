/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import org.scalatest._


/** Helper to set the system properties normally provided by the environment */
trait SystemPropertySetter extends BeforeAndAfterAll { self: Suite =>

  override def beforeAll() {
    System.setProperty("server.host", "127.0.0.1")
    System.setProperty("server.port", "8112")
    System.setProperty("app.env", "TEST")
    super.beforeAll()
  }
}
