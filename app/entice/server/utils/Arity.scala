/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils


/** @see http://stackoverflow.com/a/22181564 */
case class Arity[P](get: Int)

object Arity {
  def apply[P](implicit arity: Arity[P]) = arity
  implicit def tuple2[A,B] = Arity[(A,B)](2)
  implicit def tuple3[A,B,C] = Arity[(A,B,C)](3)
  implicit def tuple4[A,B,C,D] = Arity[(A,B,C,D)](4)
  implicit def tuple5[A,B,C,D,E] = Arity[(A,B,C,D,E)](5)
  implicit def tuple6[A,B,C,D,E,F] = Arity[(A,B,C,D,E,F)](6)
  implicit def tuple7[A,B,C,D,E,F,G] = Arity[(A,B,C,D,E,F,G)](7)
  implicit def tuple8[A,B,C,D,E,F,G,H] = Arity[(A,B,C,D,E,F,G,H)](8)
  implicit def tuple9[A,B,C,D,E,F,G,H,I] = Arity[(A,B,C,D,E,F,G,H,I)](9)
  // ... omitted, since not used
}
