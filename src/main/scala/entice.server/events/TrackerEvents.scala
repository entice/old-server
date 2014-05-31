/**
 * For copyright information see the LICENSE document.
 * Adapted from: https://gist.github.com/DeLongey/3757237
 */

package entice.server
package events

import world.{ Entity, Component }


sealed trait Update

// World, or entity-view scale:
case class EntityAdd(entity: Entity) extends Update
case class EntityRemove(entity: Entity) extends Update

// Entity scale:
case class ComponentAdd(entity: Entity, component: Component) extends Update // note: no typelevel shit possible here
case class ComponentRemove(entity: Entity, component: Component) extends Update

// Component scale:
case class ComponentChange(entity: Entity, older: Component, newer: Component) extends Update