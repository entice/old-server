/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.cores

import entice.server.implementation.events.EventBus

import akka.actor.ActorSystem

import org.scalatest._

import java.io._
import java.io.FileNotFoundException


class DefaultCoreSpec
    extends WordSpec
    with Matchers {


  /** Component under test */
  trait FakeFileDefaultCore
      extends DefaultCore {

    override lazy val akkaConfigFile = "tmp/default-core-test.conf"

    def createFile() {
      new File("tmp").mkdir
      val p = new PrintWriter("tmp/default-core-test.conf")
      p.write("akka { testValue = 1337 }")
      p.close()
    }

    def deleteFile() {
      new File("tmp/default-core-test.conf").delete()
    }
  }


  "A default server core" should {

    "load up an akka system" in new DefaultCore {
      actorSystem shouldBe an[ActorSystem]
    }

    "load up a global event bus" in new DefaultCore {
      eventBus shouldBe an[EventBus]
    }

    "load up an akka system with changed config" in new FakeFileDefaultCore {
      createFile()
      actorSystem.settings.config.getInt("akka.testValue") should be(1337)
      deleteFile()
    }

    "not load up an akka system with nonexisting config" in new FakeFileDefaultCore {
      // Note: FileInputStream here throws this instead of a FileNotFoundException
      an [StackOverflowError] should be thrownBy {
        actorSystem.settings.config.getInt("akka.testValue") should be(1337)
      }
    }
  }
}
