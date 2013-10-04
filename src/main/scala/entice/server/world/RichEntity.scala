/**
 * For copyright information see the LICENSE document.
 */

package entice.server.world

import entice.protocol._
import scala.reflect.runtime.universe._
import scala.language.postfixOps
import scala.language.implicitConversions


object RichEntity {
    implicit def strip(rich: RichEntity): Entity = rich.entity
}


/**
 * Convenience class of a standard entity (being a uuid basically)
 */
case class RichEntity private[world] (val entity: Entity, val world: World) {
    
    def comps = world.get(entity)_2
    
    def get[T <: Component : TypeTag] = comps[T]
    
    def set[T <: Component : TypeTag](newVal: T) = {
        comps.get[T] match {
            case Some(oldVal) if (oldVal == newVal) => 
                false
            case _ => 
                world.update(this, comps + newVal)
                true
        }
    }
}