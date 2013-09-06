/**
 * For copyright information see the LICENSE document.
 */

package entice.server.game

import entice.protocol._


class EntityManager {

    private var compsOfEntity:      Map[Entity, Set[Component]]                        = Map()
    private var entityOfComp:       Map[Component, Entity]                             = Map()
    private var compsOfCompType:    Map[Class[_ <: Component], Map[Entity, Component]] = Map()


    /**
     * Fluent method.
     */
    def register(entity: Entity, component: Component): EntityManager = {

        var entityComps   = compsOfEntity.getOrElse(entity, Set())
        var compTypeComps = compsOfCompType.getOrElse(component.getClass, Map())

        entityComps   = entityComps + component
        compTypeComps = compTypeComps + (entity -> component)

        // update all hashmaps
        compsOfEntity   = compsOfEntity     + (entity                -> entityComps)
        entityOfComp    = entityOfComp      + (component             -> entity)
        compsOfCompType = compsOfCompType   + (component.getClass    -> compTypeComps)

        this
    }


    /**
     * Convenience method.
     */
    def +(entity: Entity, component: Component): EntityManager = {
        register(entity, component)

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


    def getCompBy(entity: Entity, compClazz: Class[_ <: Component]) = {
        compsOfCompType.getOrElse(compClazz, Map()).get(entity)
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
}