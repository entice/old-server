/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world

import entice.server.utils._
import entice.protocol._
import shapeless._
import scala.language.postfixOps


/**
 * Manages all available systems and entities.
 */
class World(val name: String) {

    private var entities: Map[Entity, (RichEntity, TypedSet[Component])] = Map()
    private var systems:  Map[System[HList], Set[RichEntity]] = Map()

    private var changed:  Map[Entity, TypedSet[Component]] = Map()
    private var added:    List[Entity] = Nil
    private var removed:  List[Entity] = Nil


    def create(comps : TypedSet[Component]) = use(Entity(UUID()), comps)


    def use(entity: Entity, comps : TypedSet[Component]) = {
        val rich = RichEntity(entity, this)

        // ask the systems
        systems.keys 
            .filter {s: System[HList] => s.takes(comps)} 
            .map    {s: System[HList] => systems = systems + (s -> (systems(s) + rich))}

        // register
        entities = entities + (entity -> (rich, comps))

        // diffing
        changed = changed + (entity -> comps.deepClone)
        added = entity :: added

        rich
    }


    def getRich(entity: Entity) = {
        entities(entity)_1
    }


    def getComps(entity: Entity) = {
        entities(entity)_2
    }


    def remove(rich: RichEntity) { remove(rich.entity) }


    def remove(entity: Entity) {

        if (!entities.contains(entity)) return

        val rich = getRich(entity)

        // remove from the entities
        entities = entities - entity

        // remove from the systems that had it before
        systems.keys.foreach { s: System[HList] => systems = systems + (s -> (systems(s) - rich)) }

        // diffing
        removed = entity :: removed
    }


    def update(rich: RichEntity, comps: TypedSet[Component]) { update(rich.entity, comps) }


    def update(entity: Entity, comps: TypedSet[Component]) {
        val rich = entities.get(entity).get _1

        // ask the systems (remove from those that had it before)
        systems.keys
            .filterNot {s: System[HList] => s.takes(comps)}
            .foreach   {s: System[HList] => systems = systems + (s -> (systems(s) - rich))}
        systems.keys
            .filter    {s: System[HList] => s.takes(comps)}
            .foreach   {s: System[HList] => systems = systems + (s -> (systems(s) + rich))}

        // diffing
        val newComps = comps diff rich.comps 
        changed = changed + 
            (entity -> newComps.deepClone)

        // register
        entities = entities + (rich.entity -> (rich, comps))
    }


    def dump = {
        (for ((entity, (rich, comps)) <- entities)
         yield EntityView(entity, AllCompsView(comps.toList)))
        .toList
    }


    def diff: (List[EntityView], List[Entity], List[Entity]) = {
        val result = (
            (for ((e, c) <- changed) yield EntityView(e, AllCompsView(c.toList))).toList,
            added,
            removed)

        changed = Map()
        added = Nil
        removed = Nil

        result
    }


    private[world] def process(me: System[HList]) = {
        systems.get(me) match {
            case Some(e) => e
            case None =>
                register(me)
                systems.get(me).get
        }
    }


    private def register(sys: System[HList]) = {
        val e = entities.keys filter {e: Entity => sys.takes(entities(e)_2)} map {entities(_)_1}
        systems = systems + (sys -> e.toSet)
    }
}