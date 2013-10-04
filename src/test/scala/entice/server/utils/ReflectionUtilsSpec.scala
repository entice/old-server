/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.utils

import shapeless._
import scala.reflect.runtime.universe._

import org.scalatest._
import org.scalatest.matchers._


class ReflectionUtilsSpec extends WordSpec with MustMatchers  {

    import ReflectionUtils._

    case class WithInt(s: String, i: Int, b: Boolean)
    case class WithoutInt(s: String, f: Float, b: Boolean)
    case class WithIntString(s: String, i: Int, b: Boolean)
    case class WithoutIntString(f: Float, b: Boolean)


    "The ReflectionUtils" must {


        "get the type of a scala object" in {
            val i: Int = 1
            val it: Type = typeOf[Int]
            getType(i) must be(it)
        }


        "check if a list of types contains a certain type" in {
            val typesWithInt = typeOf[String :: Int :: Boolean :: HNil]
            val typesWithoutInt = typeOf[String :: Float :: Boolean :: HNil]
            contains(typesWithInt, typeOf[Int]) must be(true)
            contains(typesWithoutInt, typeOf[Int]) must be(false)
        }


        "check if a case class properties contains a certain type" in {
            val withInt = WithInt("foo", 1, true)
            val withoutInt = WithoutInt("foo", 3.42F, true)
            ccontains(withInt, typeOf[Int]) must be(true)
            ccontains(withoutInt, typeOf[Int]) must be(false)
        }


        "check if a hlist instance contains a certain type" in {
            val withBool = "foo" :: 1 :: true :: HNil
            val withoutBool = "foo" :: 1 :: 3.42F :: HNil
            hcontains(withBool, typeOf[Boolean]) must be(true)
            hcontains(withoutBool, typeOf[Boolean]) must be(false)
        }


        "check if a list of types contains all given types" in {
            val typesWithIntString = typeOf[String :: Int :: Boolean :: HNil]
            val typesWithoutIntString = typeOf[Float :: Boolean :: HNil]
            containsAll(typesWithIntString, typeOf[Int :: String :: HNil]) must be(true)
            containsAll(typesWithoutIntString, typeOf[Int :: String :: HNil]) must be(false)
        }


        "check if a case class properties contains all given types" in {
            val withIntString = WithIntString("foo", 1, true)
            val withoutIntString = WithoutIntString(3.42F, true)
            ccontainsAll(withIntString, typeOf[Int :: String :: HNil]) must be(true)
            ccontainsAll(withoutIntString, typeOf[Int :: String :: HNil]) must be(false)
        }


        "check if a hlist instance contains all given types" in {
            val withIntBool = "foo" :: 1 :: true :: HNil
            val withInt = "foo" :: 1 :: 3.42F :: HNil
            hcontainsAll(withIntBool, typeOf[Int :: Boolean :: HNil]) must be(true)
            hcontainsAll(withInt, typeOf[Int :: Boolean :: HNil]) must be(false)
        }


        "get a list of types for a given hlist-type" in {
            val listType = typeOf[String :: Int :: Boolean :: HNil]
            val expected = List(typeOf[String], typeOf[Int], typeOf[Boolean])
            htoTypes(listType) must be(expected)
        }
    }
}
