/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.attributes._
import entice.server.models._

import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._

import scala.concurrent._
import scala.concurrent.duration._


/**
 * Seeds the DB
 */
trait Seed {
  self: Core
    with Config
    with Accounts
    with Characters =>

  object seeder {

    /** All-in-one functionality */
    def seed() {
      if (environment != Test) {
        if (config.dbClean) clean()
        if (config.dbSeed && environment == Development) seedDevelopment()
        if (config.dbSeed && environment == Production) seedProduction()
      }
    }

    /** Cleaning the db only */
    def clean() {
      Logger.info("Cleaning the database...")
      val timeout: FiniteDuration = DurationInt(10).seconds
      Await.ready(accounts.dropCollection(), timeout)
      Await.ready(characters.dropCollection(), timeout)
      Logger.info("Cleaning done.")
    }

    private def seedDevelopment() {
      Logger.info("Seeding a development database...")
      for {
        acc1 <- accounts.createOrUpdate(Json.obj("email" -> "root@entice.ps"), Account("root@entice.ps", "root"))
        acc2 <- accounts.createOrUpdate(Json.obj("email" -> "test@entice.ps"), Account("test@entice.ps", "test"))
        char1 <- characters.createOrUpdateByName("Test Char",   Character(acc1.id, "Test Char", Appearance()))
        char2 <- characters.createOrUpdateByName("Abc Def",     Character(acc1.id, "Abc Def", Appearance()))
        char3 <- characters.createOrUpdateByName("Hello Again", Character(acc2.id, "Hello Again", Appearance()))
        char4 <- characters.createOrUpdateByName("Hans Wurst",  Character(acc2.id, "Hans Wurst", Appearance()))
      } {
        Logger.info("Seeding done.")
      }
    }

    private def seedProduction() {
      Logger.info("Seeding a production database...")
      for {
        acc1 <- accounts.createOrUpdate(Json.obj("email" -> "root@entice.ps"), Account("root@entice.ps", "root"))
      } {
        Logger.info("Seeding done.")
      }
    }
  }
}
