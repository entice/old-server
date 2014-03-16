/**
 * For copyright information see the LICENSE document.
 */

package entice.server.database

import entice.server.utils._

import entice.protocol._
import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.dao._
import com.novus.salat.annotations._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection
import com.mongodb.WriteConcern


object CharacterDAO extends SalatDAO[Character, ObjectId](collection = MongoConnection()(Config.get.database)("characters"))


case class Character(
    @Key("_id") id: ObjectId = new ObjectId,
    accountId: ObjectId,
    name: Name = Name("John Wayne"), 
    appearance: Appearance = Appearance())


object Character {
    def create(obj: Character) = CharacterDAO.save(obj)
    def read  (obj: Character) = CharacterDAO.findOne(MongoDBObject("_id" -> obj.id))
    def update(obj: Character) = CharacterDAO.update(
        q = MongoDBObject("_id" -> obj.id),
        t = obj,
        upsert = false,
        multi = false,
        wc = new WriteConcern()
    )
    def delete(obj: Character) = CharacterDAO.remove(MongoDBObject("_id" -> obj.id))
    

    def findByAccount(obj: Account) = CharacterDAO.find(MongoDBObject("accountId" -> obj.id)).toList
    def findByName(name: Name) = CharacterDAO.findOne(MongoDBObject("name.name" -> name.name))
    def deleteByName(name: Name) = CharacterDAO.findOne(MongoDBObject("name.name" -> name.name)).map(delete(_))
}
