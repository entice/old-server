/**
 * For copyright information see the LICENSE document.
 */

package entice.server.test

import akka.testkit.TestKit
import org.specs2.time.NoTimeConversions
import org.specs2.specification.AfterExample
import org.specs2.specification.script.SpecificationLike
import akka.actor.ActorSystem


abstract class AbstractTestKit(s: String) extends TestKit(ActorSystem(s))
  with AfterExample {

   override def after {
    system.shutdown
    system.awaitTermination
  }
}
