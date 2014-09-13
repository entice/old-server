/**
 * For copyright information see the LICENSE document.
 */

package models

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
  def nullApp = FakeApplication(withGlobal = Some(new GlobalSettings() {}))

  trait WithCharacters
      extends Characters {

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

    "get characters by account" in new WithApplication(nullApp) with WithCharacters {
      init() // TODO manually due to some strange reason with WithApplication
      characters.findByAccount(accId1) should contain(char1, char2).await
      characters.findByAccount(accId2) should contain(char3).await
      characters.findByAccount(ObjectID()) should beEmpty[List[Character]].await
    }

    "get characters by name" in new WithApplication(nullApp) with WithCharacters {
      init() // TODO manually due to some strange reason with WithApplication
      characters.findByName("Test Char 1") should beSome(char1).await
      characters.findByName("Test Char 2") should beSome(char2).await
      characters.findByName("No Char") should beNone.await
    }

    "delete characters by name" in new WithApplication(nullApp) with WithCharacters {
      init() // TODO manually due to some strange reason with WithApplication
      characters.deleteByName("Test Char 1") should beTrue.await
      characters.findByName("Test Char 1") should beNone.await
    }

    "create or update characters by name" in new WithApplication(nullApp) with WithCharacters {
      init() // TODO manually due to some strange reason with WithApplication
      val newChar1 = char1.copy(name = "Rofl Lol")
      val newChar2 = Character(accId1, "Test Char 2 Reloaded", Appearance())
      val expectedNewChar2 = char2.copy(name = "Test Char 2 Reloaded")
      // just update
      characters.createOrUpdateByName("Test Char 1", newChar1) should ===(newChar1).await
      characters.findByName("Rofl Lol") should beSome(newChar1).await
      // just create
      characters.createOrUpdateByName("Test Char 2", newChar2) should ===(expectedNewChar2).await
      characters.findByName("Test Char 2 Reloaded") should beSome(expectedNewChar2).await
    }
  }
}
