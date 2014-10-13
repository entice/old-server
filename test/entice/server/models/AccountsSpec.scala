/**
 * For copyright information see the LICENSE document.
 */

package entice.server.models

import play.api._
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent._, duration._

import org.specs2.mutable._
import org.specs2.execute._
import org.specs2.runner._
import org.junit.runner.RunWith


@RunWith(classOf[JUnitRunner])
class AccountsSpec extends Specification {
  val timeout: FiniteDuration = DurationInt(10).seconds
  def nullApp = FakeApplication(withGlobal = Some(new GlobalSettings() {}))

  trait WithAccounts
      extends Accounts {

    val acc1 = Account("test1@test.test", "passwd")
    val acc2 = Account("test2@test.test", "passwd")

    def init() {
      Await.ready(accounts.dropCollection(), timeout)
      accounts.create(acc1) should be(acc1).await
      accounts.create(acc2) should be(acc2).await
    }
  }

  "An account collection" should {

    "get accounts by email" in new WithApplication(nullApp) with WithAccounts {
      init() // TODO manually due to some strange reason with WithApplication
      accounts.findByEmail("test1@test.test") should beSome(acc1).await
      accounts.findByEmail("test2@test.test") should beSome(acc2).await
      accounts.findByEmail("none@none.test") should beNone.await
    }
  }
}
