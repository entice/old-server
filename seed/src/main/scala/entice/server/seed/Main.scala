/**
 * For copyright information see the LICENSE document.
 */

package entice.server.seed

import entice.protocol._
import entice.server.database._
import entice.server.utils._

import com.mongodb.casbah.MongoConnection


object Main extends App {
    seed
    
    def seed() {
        // some user input stuf
        println("Resetting & reseeding the database... [Y/n]?")

        if (readLine.toLowerCase != "y") { 
            println("Quitting the seeder. No harm done.")
            return 
        }

        println("... dropping database")
        MongoConnection()(Config.get.database).dropDatabase()

        // seeding admin (root) account
        println("... seeding root-account: 'root@entice.ps', pw: 'root'")
        val rootAcc = Account(email = "root@entice.ps", password = "root")
        val rootChar = Character(accountId = rootAcc.id, name = Name("To Entice Someone"))

        Account.create(rootAcc)
        Character.create(rootChar)

        // seeding test account
        println("... seeding test-account: 'test@entice.ps', pw: 'test'")
        val testAcc = Account(email = "test@entice.ps", password = "test")
        val testChar = Character(accountId = testAcc.id, name = Name("Test The Test"))

        Account.create(testAcc)
        Character.create(testChar)

        println("Seeding done. All done.'")
    }
}