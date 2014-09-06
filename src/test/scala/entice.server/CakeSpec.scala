/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import org.scalatest._


class CakeSpec
    extends WordSpec
    with Matchers
    with SystemPropertySetter {

  "An entice server" should {

    "start up" in new DefaultServer {
      serverHost should be("127.0.0.1")
      serverPort should be(8112)
      environment should be(Test)
    }
  }
}
