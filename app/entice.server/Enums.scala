/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.implementation.utils.Coord2D

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


/**
 * State of a moving entity
 */
object MoveState extends Enumeration {

  type MoveState = Value

  val NotMoving               = Value("notMoving")
  val Moving                  = Value("moving")

  implicit def enumFormat: Format[MoveState] = EnumUtils.enumFormat(MoveState)
}


/**
 * The chat channel for a channel-based chat message
 */
object ChatChannel extends Enumeration {

  type ChatChannel = Value

  val All                     = Value("all")
  val Group                   = Value("group")
  // more is yet to come ;)

  implicit def enumFormat: Format[ChatChannel] = EnumUtils.enumFormat(ChatChannel)
}


/**
 * Some animations (emotes)
 * Duration is the millisecond duration of the animation until no
 * animation is shown anymore. Special values:
 * * -1 ms: Animation is shown until client requests it to be cancelled
 * * 0  ms: Animation is shown with default time (1sec) assuming the client
 *          quits the animation automatically anyway
 * * >0 ms: Animation is shown for the secified amount of time
 */
object CharacterAnimation extends Enumeration {

  type CharacterAnimation = Value
  // internal structure
  class AniVal(name: String, val duration: Int) extends Val(nextId, name)
  protected final def Value(name: String, duration: Int): AniVal = new AniVal(name, duration)

  // workaround for withName
  final def withAniName(name: String): AniVal = super.withName(name).asInstanceOf[AniVal]

  val None                    = Value("none",                 0)

  val Agree                   = Value("agree",                1000)
  val Attention1              = Value("attention1",           1000)
  val Attention2              = Value("attention2",           1000)
  val Attention3              = Value("attention3",           1000)
  val Beg                     = Value("beg",                  1000)
  val BothHandsUp             = Value("bothHandsUp",          1000)
  val Bow                     = Value("bow",                  1000)
  val BowHead                 = Value("bowHead",              1000)
  val CatchBreath1            = Value("catchBreath1",         1000)
  val CatchBreath2            = Value("catchBreath2",         1000)
  val Cheer1                  = Value("cheer1",               1000)
  val Cheer2                  = Value("cheer2",               1000)
  val Clap                    = Value("clap",                 1000)
  val Clapping                = Value("clapping",             1000)
  val ClappingExcited         = Value("clappingExcited",      1000)
  val Cough                   = Value("cough",                1000)
  val Dance                   = Value("dance",                1000)
  val Doh                     = Value("doh",                  1000)
  val Drums                   = Value("drums",                1000)
  val Flex                    = Value("flex",                 1000)
  val Flute                   = Value("flute",                1000)
  val Guitar                  = Value("guitar",               1000)
  val HandsOnHeadLookDown     = Value("handsOnHeadLookDown",  1000)
  val HighFive                = Value("highFive",             1000)
  val IdleChatting            = Value("idleChatting",         1000)
  val IdlePonder              = Value("idlePonder",           1000)
  val Jump                    = Value("jump",                 1000)
  val Kneel                   = Value("kneel",                1000)
  val Laugh                   = Value("laugh",                1000)
  val Paper                   = Value("paper",                1000)
  val PickMe1                 = Value("pickMe1",              1000)
  val PickMe2                 = Value("pickMe2",              1000)
  val Point                   = Value("point",                1000)
  val PointNod                = Value("pointNod",             1000)
  val Ponder                  = Value("ponder",               1000)
  val Pout                    = Value("pout",                 1000)
  val QuickFlex               = Value("quickFlex",            1000)
  val Rank                    = Value("rank",                 1000)
  val Rock                    = Value("rock",                 1000)
  val Scare                   = Value("scare",                1000)
  val Scissors                = Value("scissors",             1000)
  val Scratch                 = Value("scratch",              1000)
  val ShakeFist               = Value("shakeFist",            1000)
  val Shoo                    = Value("shoo",                 1000)
  val Shoo2                   = Value("shoo2",                1000)
  val Sit                     = Value("sit",                  1000)
  val StandUp                 = Value("standUp",              1000)
  val Taunt                   = Value("taunt",                1000)
  val Tired                   = Value("tired",                1000)
  val Violin                  = Value("violin",               1000)
  val Wave                    = Value("wave",                 1000)
  val Yawn                    = Value("yawn",                 1000)

  implicit def enumFormat: Format[CharacterAnimation] = EnumUtils.enumFormat(CharacterAnimation)
}


/**
 * All accessible maps...
 */
object WorldMap extends Enumeration {

  type WorldMap = Value
  // internal structure
  class WorldMapVal(name: String, val pmap: String, val groupSize: Int, val spawns: List[Coord2D]) extends Val(nextId, name)
  protected final def Value(name: String, pmap: String, groupSize: Int, spawns: List[Coord2D]): WorldMapVal = new WorldMapVal(name, pmap, groupSize, spawns)

  // workaround for withName
  final def withMapName(name: String): WorldMapVal = super.withName(name).asInstanceOf[WorldMapVal]

  val HeroesAscent            = Value("HeroesAscent",     "heroes_ascent.json",   8, List(Coord2D(2017, -3241)))
  val TeamArenas              = Value("TeamArenas",       "team_arenas.json",     4, List(Coord2D(-1873, 352)))
  val RandomArenas            = Value("RandomArenas",     "random_arenas.json",   1, List(Coord2D(3854, 3874)))

  import Coord2D._
  implicit def enumFormat: Format[WorldMap] = EnumUtils.enumFormat(WorldMap)
}
