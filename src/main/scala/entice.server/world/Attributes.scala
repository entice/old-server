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


trait NoPropagation extends TrackingOptions { override def notPropagated = true }
trait NoVisibility  extends TrackingOptions { override def notVisible = true }


case class Name(
    val initialName: String = "Hansus Wurstus") 
    extends Attribute {

  val name = Agent(initialName)
}


case class Position(
    val initialPos: Coord2D = Coord2D(0, 0)) 
    extends Attribute {

  val pos = Agent(initialPos)
}


case class Vision(
    val initialSees: Map[Entity, Float] = Map()) 
    extends Attribute with NoPropagation{

  val sees = Agent(initialSees) // entity + distance :)
}


case class Movement(
    val initialGoal: Coord2D = Coord2D(1, 1),
    val initialState: MoveState.Value = MoveState.NotMoving) 
    extends Attribute {

  val goal = Agent(initialGoal)
  val state = Agent(initialState)
}


case class Appearance(
    val initialProfession: Int = 1,
    val initialCampaign: Int = 0,
    val initialSex: Int = 1,
    val initialHeight: Int = 0,
    val initialSkinColor: Int = 3,
    val initialHairColor: Int = 0,
    val initialHairstyle: Int = 7,
    val initialFace: Int = 31) 
    extends Attribute {

  val profession = Agent(initialProfession)
  val campaign = Agent(initialCampaign)
  val sex = Agent(initialSex)
  val height = Agent(initialHeight)
  val skinColor = Agent(initialSkinColor)
  val hairColor = Agent(initialHairColor)
  val hairstyle = Agent(initialHairstyle)
  val face = Agent(initialFace)
}                    


case class Animation(
    val initialId: Animations.AniVal = Animations.None)
    extends Attribute {

  val id = Agent(initialId)
}


case class GroupLeader(
    val initialMembers: List[Entity] = Nil,
    val initialInvited: List[Entity] = Nil,
    val initialJoinRequests: List[Entity] = Nil) 
    extends Attribute {

  val members = Agent(initialMembers)
  val invited = Agent(initialInvited)
  val joinRequests = Agent(initialJoinRequests)
}


case class GroupMember(
    val initialLeader: Option[Entity] = None) 
    extends Attribute {

  val leader = Agent(initialLeader)
}