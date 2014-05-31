/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package events

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe, ImplicitSender }
import com.typesafe.config.ConfigFactory

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.Await

import org.scalatest._


class EventBusSpec extends TestKit(ActorSystem(
    "evtbus-spec-sys", 
    config = ConfigFactory.parseString("""
      akka {
        loglevel = WARNING
      }""")))
    with WordSpecLike 
    with MustMatchers 
    with BeforeAndAfterAll
    with OneInstancePerTest {
  
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val actorSystem = system
  override def afterAll { TestKit.shutdownActorSystem(system) }


  "An entice event bus" must {
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
    }
  }
}


case class TestMessage()
case class GenericTestMessage[T]()