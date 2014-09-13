/**
 * For copyright information see the LICENSE document.
 */

package models

import play.api._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent._
import duration._

import org.specs2.mutable._
import org.specs2.execute._
import org.specs2.runner._
import org.junit.runner.RunWith


@RunWith(classOf[JUnitRunner])
class CollectionSpec extends Specification {
  sequential
  val timeout: FiniteDuration = DurationInt(10).seconds
  def nullApp = FakeApplication(withGlobal = Some(new GlobalSettings() {}))

  case class Nested(foo: Boolean = true)

  case class TestDAO(
    name: String = "Some Thing",
    age: Int = 42,
    nested: Nested = Nested(),
    id: ObjectID = ObjectID()) extends DataAccessType

  implicit val nestedFormat = Json.format[Nested]
  implicit val testDAOFormat = Json.format[TestDAO]

  trait WithCollection {

    lazy val testCollection = new Collection[TestDAO]("test-daos")

    val dao1 = TestDAO(name = "Hans Wurst")
    val dao2 = TestDAO(age = 1337)
    val dao3 = TestDAO(nested = Nested(false))

    def init() {
      Await.ready(testCollection.dropCollection(), timeout)
      testCollection.create(dao1) should be(dao1).await
      testCollection.create(dao2) should be(dao2).await
      testCollection.create(dao3) should be(dao3).await
    }
  }

  "A general collection" should {

    "do findById" in new WithApplication(nullApp) with WithCollection {
      init() // TODO manually due to some strange reason with WithApplication
      testCollection.findById(dao1.id) should beSome(dao1).await
      testCollection.findById(dao2.id) should beSome(dao2).await
      testCollection.findById(ObjectID()) should beNone.await
    }

    "do findByQuery" in new WithApplication(nullApp) with WithCollection {
      init() // TODO manually due to some strange reason with WithApplication
      testCollection.findByQuery(Json.obj("name" -> "Hans Wurst")) should contain(dao1).await
      testCollection.findByQuery(Json.obj("name" -> "Some Thing")) should contain(dao2, dao3).await
      testCollection.findByQuery(Json.obj("age" -> 1337)) should contain(dao2).await
      testCollection.findByQuery(Json.obj("nested.foo" -> false)) should contain(dao3).await
      testCollection.findByQuery(Json.obj("age" -> 0)) should beEmpty[List[TestDAO]].await
    }

    "do update by ID" in new WithApplication(nullApp) with WithCollection {
      init() // TODO manually due to some strange reason with WithApplication
      val newDao1 = dao1.copy(name = "Hansus Wurstus")
      val newDao2 = dao2.copy(name = "Kacktus Stechus")
      val other = TestDAO(name = "Lol Rofl")
      testCollection.update(newDao1) should beSome(newDao1).await
      testCollection.findById(dao1.id) should beSome(newDao1).await
      testCollection.update(newDao2) should beSome(newDao2).await
      testCollection.findById(dao2.id) should beSome(newDao2).await
      testCollection update(other) should beNone.await // passes through but
      testCollection.findById(other.id) should beNone.await // will not be added
    }

    "do update by Query" in new WithApplication(nullApp) with WithCollection {
      init() // TODO manually due to some strange reason with WithApplication
      val newDao1 = dao1.copy(name = "Hansus Wurstus")
      val newDao2 = TestDAO(name = "Lol Rofl")
      val expectedNewDao2 = newDao2.copy(id = dao2.id)
      val newDao3 = dao3.copy(nested = Nested())
      // normal update
      testCollection.update(Json.obj("name" -> "Hans Wurst"), newDao1) should beSome(newDao1).await
      testCollection.findById(dao1.id) should beSome(newDao1).await
      // exchange whole object
      testCollection.update(Json.obj("age" -> 1337), newDao2) should beSome(expectedNewDao2).await
      testCollection.findById(dao2.id) should beSome(expectedNewDao2).await
      testCollection.findById(newDao2.id) should beNone.await
      // exchange when nested
      testCollection update(Json.obj("nested.foo" -> false), newDao3) should beSome(newDao3).await
      testCollection.findById(dao3.id) should beSome(newDao3).await
    }

    "do createOrUpdate by ID" in new WithApplication(nullApp) with WithCollection {
      init() // TODO manually due to some strange reason with WithApplication
      val newDao1 = dao1.copy(name = "Hansus Wurstus")
      val newDao2 = dao2.copy(name = "Kacktus Stechus")
      val other = TestDAO(name = "Lol Rofl")
      testCollection.createOrUpdate(newDao1) should ===(newDao1).await
      testCollection.findById(dao1.id) should beSome(newDao1).await
      testCollection.createOrUpdate(newDao2) should ===(newDao2).await
      testCollection.findById(dao2.id) should beSome(newDao2).await
      testCollection createOrUpdate(other) should ===(other).await // passes through but
      testCollection.findById(other.id) should beSome(other).await // will not be added
    }

    "do createOrUdate by Query" in new WithApplication(nullApp) with WithCollection {
      init() // TODO manually due to some strange reason with WithApplication
      val newDao1 = dao1.copy(name = "Hansus Wurstus")
      val newDao2 = TestDAO(name = "Kacktus Stechus")
      val expectedNewDao2 = newDao2.copy(id = dao2.id)
      val other = TestDAO(name = "Lol Rofl")
      // normal update
      testCollection.createOrUpdate(Json.obj("name" -> "Hans Wurst"), newDao1) should ===(newDao1).await
      testCollection.findById(dao1.id) should beSome(newDao1).await
      // exchange whole object
      testCollection.createOrUpdate(Json.obj("age" -> 1337), newDao2) should ===(expectedNewDao2).await
      testCollection.findById(dao2.id) should beSome(expectedNewDao2).await
      testCollection.findById(newDao2.id) should beNone.await
      // exchange when nested
      testCollection createOrUpdate(Json.obj("age" -> 0), other) should ===(other).await
      testCollection.findById(other.id) should beSome(other).await
    }

    "do delete" in new WithApplication(nullApp) with WithCollection {
      init() // TODO manually due to some strange reason with WithApplication
      testCollection.delete(dao1.id) should beTrue.await
      testCollection.findById(dao1.id) should beNone.await
    }
  }
}
