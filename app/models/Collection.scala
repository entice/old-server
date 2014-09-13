/**
 * For copyright information see the LICENSE document.
 */

package models

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


/** A simple ObjectID helper. This is used to internally reflect BSON-ObjectIDs */
case class ObjectID($oid: String = BSONObjectID.generate.stringify)
object ObjectID {
  implicit val oidFormat = Json.format[ObjectID]
}
import ObjectID._


/**
 * Contract for DAOs. Implement it like:
 * case class Coffee(name: String, price: Int, id: ObjectID = ObjectID())
 */
trait DataAccessType { def id: ObjectID }


/** A mongoDB collection. Implement your DB logic based on this */
class Collection[DAO <: DataAccessType : Format](collectionName: String) {
  import Play.current
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

  def update(dao: DAO): Future[Option[DAO]] = {
    val jsonDao = toMongo(Json.toJson(dao)).as[JsObject]
    val selector = Json.obj("_id" -> (jsonDao \ "_id"))
    val modifier = Json.obj("$set" -> (jsonDao - "_id"))
    collection.update(selector, modifier).flatMap { _ => findById(dao.id) }
  }

  /** TODO: very inefficient, use FindAndModify? */
  def update(query: JsObject, dao: DAO): Future[Option[DAO]] = {
    val jsonDao = toMongo(Json.toJson(dao)).as[JsObject]
    findByQuery(query).flatMap { _.headOption match {
      case Some(d) =>
        val selector = Json.obj("_id" -> d.id)
        val modifier = Json.obj("$set" -> (jsonDao - "_id"))
        collection.update(selector, modifier).flatMap { _ => findById(d.id) }
      case None => Future.successful(None)
    }}
  }

  def createOrUpdate(dao: DAO): Future[DAO] = {
    findById(dao.id).flatMap { _ match {
      case Some(d) => update(dao).map { _.get }
      case None    => create(dao)
    }}
  }

  def createOrUpdate(query: JsObject, dao: DAO): Future[DAO] = {
    findByQuery(query).flatMap { _.headOption match {
      case Some(d) => update(query, dao).map { _.get }
      case None    => create(dao)
    }}
  }

  def delete(id: ObjectID): Future[Boolean] = {
    collection.remove(Json.obj("_id" -> id)).map { x => true }
  }

  def dropCollection(): Future[Boolean] = {
    collection.drop().map { x => true }
  }
}
