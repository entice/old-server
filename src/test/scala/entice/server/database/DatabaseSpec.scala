/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.database

import entice.protocol._
import org.scalatest._
import org.scalatest.matchers._


class DatabaseSpec extends WordSpec with MustMatchers  {


    "An entice database" must {


        "do CRUD on accounts and extras" in {
            val acc = Account(email = "dbaccspec@entice.org", password = "test")

            // C (reate)
            Account.create(acc)
            // R (read)
            Account.read(acc) must be(Some(acc))
            // U (update)
            val acc2 = acc.copy(password = "test2")
            Account.update(acc2)
            Account.read(acc) must be(Some(acc2))
            // D (elete)
            Account.delete(acc)
            Account.read(acc) must be(None)

            // extras
            Account.create(acc)
            Account.findByEmail("dbaccspec@entice.org") must be(Some(acc))

            // cleanup
            Account.delete(acc)
        }


        "do CRUD on characters and extras" in {
            val acc = Account(email = "dbcharspec@entice.org", password = "test")
            val char = Character(accountId = acc.id, name = Name("db-char-spec-test"))

            // C (reate)
            Character.create(char)
            // R (read)
            Character.read(char) must be(Some(char))
            // U (update)
            val char2 = char.copy(name = Name("db-char-spec-test2"))
            Character.update(char2)
            Character.read(char) must be(Some(char2))
            // D (elete)
            Character.delete(char)
            Character.read(char) must be(None)

            // extras
            val char3 = Character(accountId = acc.id, name = Name("db-char-spec-test3"))
            Character.create(char)
            Character.create(char3)
            Character.findByAccount(acc) must be(List(char, char3))
            Character.findByName(Name("db-char-spec-test")) must be(Some(char))

            // cleanup
            Character.delete(char)
            Character.delete(char3)
        }
    }
}