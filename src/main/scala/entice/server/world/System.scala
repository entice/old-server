/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world

import entice.protocol._
import entice.server.utils._, ReflectionUtils._
import shapeless._
import scala.reflect.runtime.universe._


/**
 * Supertype of all systems.
 * Convenience type: Put an HList of accepted components in the typeparam,
 * and you will get a method that checks if a certain entity's components
 * conform to that. (I.e. all component types must be present to be able to
 * use the entity with this system)
 *
 * Usage example:
 * class MovementSystem extends System[Position :: Movement :: HNil] { ... }
 * class SpawningSystem extends System[Position :: Name :: HNil] { ... }
 */
abstract class System[T <: HList : TypeTag] {

    val types = htoTypes(typeOf[T])

    def takes(comps: TypedSet[Component]) = {
        types
            .filterNot {comps.contains} 
            .isEmpty
    }

    def entities(world: World) = world.process(this.asInstanceOf[System[HList]])

    def update(world: World) {}
}