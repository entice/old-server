/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import Named._

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.Await

import org.scalatest._


class SystemSpec extends WordSpecLike with MustMatchers {

  "TakesComponents" must {

    "take only entities with specific components" in {
      val w = new World {}
      val e1 = new Entity(w) + SomeComp1()
      val e2 = new Entity(w) + SomeComp1() + SomeComp2()
      val e3 = new Entity(w) + SomeComp1() + SomeComp3()
      val s1 = SomeSystem1()
      val s2 = SomeSystem2()
      val s3 = SomeSystem3()

      s1.takes(e1) must be(true)
      s1.takes(e2) must be(true)
      s1.takes(e3) must be(true)

      s2.takes(e1) must be(true)
      s2.takes(e2) must be(false)
      s2.takes(e3) must be(true)

      s3.takes(e1) must be(false)
      s3.takes(e2) must be(false)
      s3.takes(e3) must be(true)
    }
  }
}

case class SomeComp1() extends Component
case class SomeComp2() extends Component 
case class SomeComp3() extends Component 

case class SomeSystem1() extends TakesComponents {
  val requires = has[SomeComp1] :: Nil
}

case class SomeSystem2() extends TakesComponents {
  val requires = has[SomeComp1] :: hasNot[SomeComp2] :: Nil
}

case class SomeSystem3() extends TakesComponents {
  val requires = has[SomeComp1] :: has[SomeComp3] :: Nil
}