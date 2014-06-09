/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package world

import entice.protocol._

import akka.agent.Agent
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection._


trait Attribute extends mutable.Cloneable[Attribute] with TrackingOptions


case class Name() extends Attribute {
  val name = Agent("Hansus Wurstus")
}


case class Position() extends Attribute {
  val pos = Agent(Coord2D(0, 0))
}


case class Vision() extends Attribute {
  val sees = Agent(Map[Entity, Float]()) // entity + distance :)

  override def notPropagated = true
}


case class Movement() extends Attribute {
  val goal = Agent(Coord2D(1, 1))
  val state = Agent(MoveState.NotMoving)
}


case class Appearance() extends Attribute {
  val profession = Agent(1)
  val campaign = Agent(0)
  val sex = Agent(1)
  val height = Agent(0)
  val skinColor = Agent(3)
  val hairColor = Agent(0)
  val hairstyle = Agent(7)
  val face = Agent(31)
}                    


case class Animation() extends Attribute {
  val id = Agent(Animations.None)
}


case class GroupLeader() extends Attribute {
  val members = Agent(List[Entity]())
  val invited = Agent(List[Entity]())
  val joinRequests = Agent(List[Entity]())
}


case class GroupMember() extends Attribute {
  val leader = Agent(None: Option[Entity])
}