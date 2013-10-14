/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world

import entice.server.utils._
import entice.protocol._
import shapeless._
import scala.language.postfixOps


/**
 * Manages all entities registered to it, all systems registered to it, and can
 * create diffs of its state transitions.
 */
class World(val name: String) extends WorldCore with SystemsManagement with DiffManagement


/**
 * Manages all entities registered to it.
 */
private[world] trait WorldCore {
    self: World =>

    protected var entities: Map[Entity, (RichEntity, TypedSet[Component])] = Map()


    def create(comps : TypedSet[Component]) = use(Entity(UUID()), comps)

    def use(entity: Entity, comps : TypedSet[Component]) = {
        val rich = RichEntity(entity, self)
        entities = entities + (entity -> ((rich, comps)))
        rich
    }


    def remove(rich: RichEntity) { remove(rich.entity) }
    def remove(entity: Entity)   { entities = entities - entity }


    def update(entity: Entity, comps: TypedSet[Component]) {
        if (!entities.contains(entity)) return
        update(getRich(entity).get, comps)
    }

    def update(rich: RichEntity, comps: TypedSet[Component]) {
        if (rich.world != this) return
        entities = entities + (rich.entity -> ((rich, comps)))
    }


    def contains(entity: Entity) = entities.contains(entity)


    def getRich(entity: Entity): Option[RichEntity] = {
        entities.get(entity) match {
            case Some((rich, comps)) => Some(rich)
            case None => None
        }
    }


    def getComps(entity: Entity): Option[TypedSet[Component]] = {
        entities.get(entity) match {
            case Some((rich, comps)) => Some(comps)
            case None => None
        }
    }


    def dump: Map[Entity, TypedSet[Component]] = {
        (for ((entity, (rich, comps)) <- entities) yield
        (entity -> comps.deepClone))
        .toMap
    }    
}


