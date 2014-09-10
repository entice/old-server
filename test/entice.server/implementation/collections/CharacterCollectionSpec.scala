/**
 * For copyright information see the LICENSE document.
 */

package entice.server.implementation.collections

import entice.server._
import entice.server.implementation.attributes._

import play.api._
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent._
import duration._

import org.specs2.mutable._
import org.specs2.execute._
import org.specs2.runner._
import org.junit.runner.RunWith


@RunWith(classOf[JUnitRunner])
class CharacterCollectionSpec extends Specification {
  sequential
  val timeout: FiniteDuration = DurationInt(10).seconds

  trait WithAccounts
      extends Core
      with CharacterCollection {

    val accId1 = ObjectID()
    val accId2 = ObjectID()

    val char1 = Character(accId1, "Test Char 1", Appearance())
    val char2 = Character(accId1, "Test Char 2", Appearance())
    val char3 = Character(accId2, "Test Char 3", Appearance())

    def init() {
      Await.ready(characters.dropCollection(), timeout)
      characters.create(char1) should be(char1).await
      characters.create(char2) should be(char2).await
      characters.create(char3) should be(char3).await
    }
  }

  "A character collection" should {

    "get characters by account" in new WithApplication(FakeApplication(withGlobal = Some(new GlobalSettings() {}))) with WithAccounts {
      init() // TODO manually due to some strange reason with WithApplication
      characters.findByAccount(accId1) should contain(char1, char2).await
      characters.findByAccount(accId2) should contain(char3).await
      characters.findByAccount(ObjectID()) should beEmpty[List[Character]].await
    }

    "get characters by name" in new WithApplication(FakeApplication(withGlobal = Some(new GlobalSettings() {}))) with WithAccounts {
      init() // TODO manually due to some strange reason with WithApplication
      characters.findByName("Test Char 1") should beSome(char1).await
      characters.findByName("Test Char 2") should beSome(char2).await
      characters.findByName("No Char") should beNone.await
    }

    "delete characters by name" in new WithApplication(FakeApplication(withGlobal = Some(new GlobalSettings() {}))) with WithAccounts {
      init() // TODO manually due to some strange reason with WithApplication
      characters.deleteByName("Test Char 1") should beTrue.await
      characters.findByName("Test Char 1") should beNone.await
    }
  }
}
