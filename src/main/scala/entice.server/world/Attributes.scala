/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import akka.agent.Agent
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection._


trait Attribute extends mutable.Cloneable[Attribute] with TrackingOptions


case class Name() extends Attribute {
  val name = Agent("No Name")
}


case class Age() extends Attribute {
  val age = Agent(0)
}


case class Vision() extends Attribute {
  val sees = Agent(Map[Entity, Float]()) // entity + distance :)
}