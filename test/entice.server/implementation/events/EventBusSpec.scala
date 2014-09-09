/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.events

import entice.server.test._
import entice.server.macros._

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe, ImplicitSender }
import com.typesafe.config.ConfigFactory

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.Future

import org.specs2.mutable.SpecificationLike
import org.specs2.time.NoTimeConversions


class EventBusSpec extends AbstractTestKit("EventBusSpec") with SpecificationLike with NoTimeConversions {

  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val actorSystem = system

  import EventBusSpec._

  "An entice event bus" should {
    "subscribe and publish to the right actors" in {
      val bus = new EventBus()

      val a1 = TestProbe()
      val a2 = TestProbe()
      val a3 = TestProbe()

      { implicit val actor = a1.ref; bus.sub[TestMessage] }
      { implicit val actor = a2.ref; bus.sub[GenericTestMessage[String]] }
      { implicit val actor = a2.ref; bus.sub[GenericTestMessage[Boolean]] }
      { implicit val actor = a3.ref; bus.sub[GenericTestMessage[Boolean]] }

      {
        implicit val actor = testActor
        bus.pub(TestMessage())
        a1.expectMsg(Evt(TestMessage()))
        a2.expectNoMsg
        a3.expectNoMsg
      }


      {
        implicit val actor = testActor
        bus.pub(GenericTestMessage[String]())
        a1.expectNoMsg
        a2.expectMsg(Evt(GenericTestMessage[String]()))
        a3.expectNoMsg
      }


      {
        implicit val actor = testActor
        bus.pub(GenericTestMessage[Boolean]())
        a1.expectNoMsg
        a2.expectMsg(Evt(GenericTestMessage[Boolean]()))
        a3.expectMsg(Evt(GenericTestMessage[Boolean]()))
      }

      true
    }
  }
}

object EventBusSpec {
  case class TestMessage()
  case class GenericTestMessage[T]()
}
