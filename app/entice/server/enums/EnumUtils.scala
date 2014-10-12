/**
 * For copyright information see the LICENSE document.
 */

package entice.server.enums

import play.api.libs.json._


/**
 * Used to de/serialize enums
 * @see http://stackoverflow.com/questions/15488639/how-to-write-readst-and-writest-in-scala-enumeration-play-framework-2-1/15489179#15489179
 */
object EnumUtils {
  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = {
   new Reads[E#Value] {
     def reads(json: JsValue): JsResult[E#Value] = json match {
       case JsString(s) =>
         try {
           JsSuccess(enum.withName(s))
         } catch {
           case _: NoSuchElementException =>
             JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
         }
       case _ => JsError("String value expected")
     }
   }
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = {
   new Writes[E#Value] { def writes(v: E#Value): JsValue = JsString(v.toString) }
  }

  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
   Format(enumReads(enum), enumWrites)
  }
}
