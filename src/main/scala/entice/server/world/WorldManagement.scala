/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world

import entice.server.utils._
import entice.protocol._
import shapeless._
import scala.language.postfixOps


/**
 * Adds the ability to create diffs of this world.
 * (Where diffs are state transitions that occured between calls of the diff method)
 */
private[world] trait DiffManagement extends WorldCore {
    self: World =>

    private var lastDump: Map[Entity, TypedSet[Component]] = dump

    // current diff (we do it continously, so we dont have to do too much while diffing)
    private var changed:  Map[Entity, (TypedSet[Component], TypedSet[Component], TypedSet[Component])] = Map()


    /**
     * When updating the components of an entity, also add it to the diff.
     */
    abstract override def update(rich: RichEntity, comps: TypedSet[Component]) {
        if (!lastDump.contains(rich.entity)) { super.update(rich, comps); return }

        val entity = rich.entity 
        val oldComps = lastDump(entity)

        // due to the nature of diff, all these sets will be deepclones
        val changedComps = comps intersect oldComps
        var newComps     = comps diffNew   oldComps
        var removedComps = comps diffOld   oldComps
        
        // overwrite any other changes that we saved previously
        changed = changed + (rich.entity -> ((changedComps, newComps, removedComps)))

        super.update(rich, comps)
    }


    /**
     * Should be called by one source only!!!
     * Generates state transitions in the form of EntityViews, that occured between
     * this call and the last call.
     */
    def diff: (List[EntityView], List[Entity], List[Entity]) = {

        val currentDump = dump

        val addedEty = currentDump.keys.toList diff lastDump.keys.toList
        val removedEty = lastDump.keys.toList diff currentDump.keys.toList

        for (e <- addedEty; c <- currentDump.get(e)) {
            changed = changed + (e -> ((new TypedSet[Component](), c.deepClone, new TypedSet[Component]())))
        }

        val result = (toEntityViews(changed), addedEty, removedEty)

        lastDump = currentDump
        changed = Map()

        result
    }


    private def toEntityViews(changed: Map[Entity, (TypedSet[Component], TypedSet[Component], TypedSet[Component])]):
        List[EntityView] = {

        (for ((e, (c1, c2, c3)) <- changed) yield {
            val removed = c3.toList map {_.`type`}
            EntityView(e, c1.toList, c2.toList, removed)
        }).toList
    }
}


/**
 * Adds the ability to use systems in this world.
 */
private[world] trait SystemsManagement extends WorldCore {
    self: World =>

    private var systems:  Map[System[HList], Set[RichEntity]] = Map()


    /**
     * When using a new entity, also ask known systems if they want to use it.
     */
    abstract override def use(entity: Entity, comps : TypedSet[Component]) = {
        val rich = super.use(entity, comps)

        // ask the systems
        systems.keys 
            .filter {s: System[HList] => s.takes(comps)} 
            .map    {s: System[HList] => systems = systems + (s -> (systems(s) + rich))}

        rich
    }


    /**
     * When removing an entity, make sure that it's not registered with any system.
     */
    abstract override def remove(entity: Entity) {
        if (!super.contains(entity)) { super.remove(entity); return }

        val rich = getRich(entity).get
        systems.keys.foreach { s: System[HList] => systems = systems + (s -> (systems(s) - rich)) }

        super.remove(entity)
    }


    /**
     * When updating the components of an entity, make sure that the systems get informed,
     * so we dont have conflicts later on.
     */
    abstract override def update(rich: RichEntity, comps: TypedSet[Component]) {
        systems.keys
            .filterNot {s: System[HList] => s.takes(comps)}
            .foreach   {s: System[HList] => systems = systems + (s -> (systems(s) - rich))}
        systems.keys
            .filter    {s: System[HList] => s.takes(comps)}
            .foreach   {s: System[HList] => systems = systems + (s -> (systems(s) + rich))}

        super.update(rich, comps)
    }


    /**
     * Called by a system to get all entities that fit to the system's component
     * requirements
     */
    private[world] def process(me: System[HList]) = {
        systems.get(me) match {
            case Some(e) => e
            case None =>
                register(me)
                systems.get(me).get
        }
    }


    private[world] def register(sys: System[HList]) {
        val e = entities.keys filter {e: Entity => sys.takes(entities(e)_2)} map {entities(_)_1}
        systems = systems + (sys -> e.toSet)
    }


    private[world] def unregister(sys: System[HList]) { systems = systems - sys }
}


/**
 * Adds the ability to create events when the world-state changes.
 */
private[world] trait EventManagement extends WorldCore {
    self: World =>

    val messageBus: MessageBus

    /**
     * When using a new entity, create a spawned entity event.
     */
    abstract override def use(entity: Entity, comps : TypedSet[Component]) = {
        val rich = super.use(entity, comps)
        messageBus.publish(MessageEvent(null, Spawned(rich)))
        rich
    }


    /**
     * When removing an entity, create a despawned entity event.
     */
    abstract override def remove(entity: Entity) {
        if (!super.contains(entity)) { super.remove(entity); return }
        val rich = getRich(entity).get
        val comps = rich.comps
        messageBus.publish(MessageEvent(null, Despawned(this, entity, comps)))
        super.remove(entity)
    }
}