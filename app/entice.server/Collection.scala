/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import scala.concurrent.Future

// Reactive Mongo imports
import reactivemongo.api._
import reactivemongo.bson.BSONObjectID

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection


trait Collection { self: Core =>

  case class ObjectID($oid: String = BSONObjectID.generate.stringify)
  implicit val oidFormat = Json.format[ObjectID]

  trait DataAccessType { def id: ObjectID }

  class CollectionLike[DAO <: DataAccessType : Format](collectionName: String) {

    implicit val app = self.app
    private def db = ReactiveMongoPlugin.db
    private lazy val collection = db.collection[JSONCollection](collectionName)

    def toMongo(obj: JsValue): JsValue = { obj.asOpt[JsObject].map({ js => js - "id"  ++ Json.obj("_id" -> (js \ "id")) }).getOrElse(obj) }
    def fromMongo(obj: JsValue): JsValue = { obj.asOpt[JsObject].map({ js => js - "_id"  ++ Json.obj("id" -> (js \ "_id")) }).getOrElse(obj) }


    def create(dao: DAO): Future[DAO] = {
      val jsonDao = toMongo(Json.toJson(dao))
      collection.insert(jsonDao).map { x => dao }
    }

    def findById(id: ObjectID): Future[Option[DAO]] = {
      findByQuery(Json.obj("_id" -> id)).map { l => l.headOption }
    }

    def findByQuery(query: JsObject): Future[List[DAO]] = {
      val cursor: Cursor[JsObject] =
        collection
          .find(query)
          .cursor[JsObject]

      cursor.collect[List]()
        .map { _.flatMap { res => Json.fromJson[DAO](fromMongo(res)).asOpt } }
    }

    def update(dao: DAO): Future[DAO] = {
      val jsonDao = toMongo(Json.toJson(dao)).as[JsObject]
      val selector = Json.obj("_id" -> (jsonDao \ "_id"))
      val modifier = Json.obj("$set" -> (jsonDao - "_id"))
      collection.update(selector, modifier).map { x => dao }
    }

    def delete(id: ObjectID): Future[Boolean] = {
      collection.remove(Json.obj("_id" -> id)).map { x => true }
    }

    def dropCollection(): Future[Boolean] = {
      collection.drop().map { x => true }
    }
  }
}
