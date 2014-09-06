/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.configs

import entice.server._
import entice.server.implementation.loggers.NullLogger
import entice.server.implementation.events.EventBus

import akka.actor.ActorSystem

import com.typesafe.config._

import org.scalatest._

import java.io._
import java.io.FileNotFoundException


class JsonConfigSpec
    extends WordSpec
    with Matchers {


  /** Component under test */
  trait FakeFileJsonConfig
      extends JsonConfig { self: Logger =>

    override lazy val configFile = "tmp-test/json-config-test.json"

    def createFile() {
      new File("tmp-test").mkdir
      val p = new PrintWriter("tmp-test/json-config-test.json")
      p.write("""{
        "tick":      1337,
        "minUpdate": 50,
        "maxUpdate": 250
      }""")
      p.close()
    }

    def deleteFile() {
      new File("tmp-test/json-config-test.json").delete()
      new File("tmp-test").delete()
    }
  }


  "A json config" should {

    "load up a config" in new JsonConfig with NullLogger {
      config shouldBe a[Config]
    }

    "load up a config with changed file" in new FakeFileJsonConfig with NullLogger {
      createFile()
      config.tick should be(1337)
      deleteFile()
    }

    "load up a config with nonexisting file - by using defaults" in new FakeFileJsonConfig with NullLogger {
      config.tick should be(30)
    }
  }
}
