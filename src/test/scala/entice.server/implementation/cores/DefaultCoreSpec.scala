/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.cores

import entice.server._
import entice.server.implementation.loggers.NullLogger
import entice.server.implementation.events.EventBus

import akka.actor.ActorSystem

import com.typesafe.config._

import org.scalatest._

import java.io._
import java.io.FileNotFoundException


class DefaultCoreSpec
    extends WordSpec
    with Matchers {


  /** Component under test */
  trait FakeFileDefaultCore
      extends DefaultCore { self: Logger =>

    override lazy val akkaConfigFile = "tmp-test/default-core-test.conf"

    def createFile() {
      new File("tmp-test").mkdir
      val p = new PrintWriter("tmp-test/default-core-test.conf")
      p.write("akka { testValue = 1337 }")
      p.close()
    }

    def deleteFile() {
      new File("tmp-test/default-core-test.conf").delete()
      new File("tmp-test").delete()
    }
  }


  "A default server core" should {

    "load up an akka system" in new DefaultCore with NullLogger {
      actorSystem shouldBe an[ActorSystem]
    }

    "load up a global event bus" in new DefaultCore with NullLogger {
      eventBus shouldBe an[EventBus]
    }

    "load up an akka system with changed config" in new FakeFileDefaultCore with NullLogger {
      createFile()
      actorSystem.settings.config.getInt("akka.testValue") should be(1337)
      deleteFile()
    }

    "load up an akka system with nonexisting config - by using defaults" in new FakeFileDefaultCore with NullLogger {
      a [ConfigException.Missing] should be thrownBy {
        actorSystem.settings.config.getInt("akka.testValue")
      }
    }
  }
}
