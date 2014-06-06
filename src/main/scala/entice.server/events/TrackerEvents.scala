/**
 * For copyright information see the LICENSE document.
 * Adapted from: https://gist.github.com/DeLongey/3757237
 */

package entice.server
package events

import world.{ Entity, Attribute, TrackingOptions }


sealed trait Update extends TrackingOptions { def entity: Entity }

// World, or entity-view scope:
case class EntityAdd(entity: Entity) extends Update
case class EntityRemove(entity: Entity) extends Update

// Entity scope:
case class AttributeAdd(entity: Entity, attribute: Attribute) extends Update { // note: no typelevel shit possible here
  override def notPropagated = attribute.notPropagated
  override def notVisible = attribute.notVisible
}
case class AttributeRemove(entity: Entity, attribute: Attribute) extends Update {
  override def notPropagated = attribute.notPropagated
  override def notVisible = attribute.notVisible
}

// Attribute scope:
case class AttributeChange(entity: Entity, older: Attribute, newer: Attribute) extends Update {
  override def notPropagated = older.notPropagated
  override def notVisible = older.notVisible
}