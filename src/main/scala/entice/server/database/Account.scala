/**
 * For copyright information see the LICENSE document.
 */

package entice.server.database

import entice.server.utils._

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.dao._
import com.novus.salat.annotations._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection
import com.mongodb.WriteConcern


object AccountDAO extends SalatDAO[Account, ObjectId](collection = MongoConnection()(Config.get.database)("accounts"))


case class Account(
    @Key("_id") id : ObjectId = new ObjectId, 
    email: String, 
    password: String)


object Account {
    def create(obj: Account) = AccountDAO.save(obj)
    def read  (obj: Account) = AccountDAO.findOne(MongoDBObject("_id" -> obj.id))
    def update(obj: Account) = AccountDAO.update(
        q = MongoDBObject("_id" -> obj.id),
        t = obj,
        upsert = false,
        multi = false,
        wc = new WriteConcern()
    )
    def delete(obj: Account) = AccountDAO.remove(MongoDBObject("_id" -> obj.id))
    

    def findByEmail(email: String) = AccountDAO.findOne(MongoDBObject("email" -> email))
}
