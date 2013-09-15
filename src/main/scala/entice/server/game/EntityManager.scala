/**
 * For copyright information see the LICENSE document.
 */

package entice.server.game

import entice.protocol._


/**
 * TODO: Reduce the copying by using mutable maps and do the sync manually
 */
class EntityManager {

    private var compsOfEntity:      Map[Entity, Set[Component]]                        = Map()
    private var entityOfComp:       Map[Component, Entity]                             = Map()
    private var compsOfCompType:    Map[Class[_ <: Component], Map[Entity, Component]] = Map()


    /**
     * Fluent method.
     * All components that are regsitered together, will also be added together,
     * like in a transaction. No system will see an intermediate state with only
     * parts of the components being accessible.
     */
    def register(entity: Entity, components: Component*): EntityManager = {

        var entityComps = compsOfEntity.getOrElse(entity, Set())
        entityComps     = entityComps ++ components
        compsOfEntity   = compsOfEntity + (entity -> entityComps)

        components map { c: Component =>
            var compTypeComps = compsOfCompType.getOrElse(c.getClass, Map())
            compTypeComps = compTypeComps + (entity -> c)
            compsOfCompType = compsOfCompType + (c.getClass -> compTypeComps)
        }

        components map { c: Component => entityOfComp = entityOfComp + (c -> entity) }

        this
    }


    /**
     * Fluent convenience method.
     */
    def +(entity: Entity, components: Component*): EntityManager = {
        register(entity, components:_*)

        this
    }


    def unregister(entity: Entity) {

        var entityComps = compsOfEntity.getOrElse(entity, Nil)

        // cleanup the hashmaps
        entityComps map { c: Component =>
            entityOfComp = entityOfComp - c 
        }
        entityComps map { c: Component => 
            val entityCompAssoc = compsOfCompType.getOrElse(c.getClass, Map()) - entity
            compsOfCompType = compsOfCompType + (c.getClass -> entityCompAssoc)
        }

        compsOfEntity = compsOfEntity - entity
    }


    def getCompBy[T <: Component](entity: Entity, compClazz: Class[T]) = {
        compsOfCompType
            .getOrElse(compClazz, Map())
            .get(entity)
            .asInstanceOf[Option[T]]
    }


    def getCompsOf(entity: Entity) = {
        compsOfEntity.get(entity)
    }


    def getEntityOf(component: Component) = {
        entityOfComp.get(component)
    }


    def getEntitiesWith(compClazzes: Class[_ <: Component]*) = {
        val entitiesForComps = 
        for (compClazz <- compClazzes) 
        yield {
            compsOfCompType.getOrElse(compClazz, Map()).keys.toSet
        }

        // we want all entities that are in every sublist, i.e. that have
        // all the required components
        entitiesForComps.reduceRight { _ intersect _ }
    }


    def getAll = compsOfEntity
}