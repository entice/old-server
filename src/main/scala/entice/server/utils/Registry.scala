/**
 * For copyright information see the LICENSE document.
 */

package entice.server.utils

import entice.protocol._


/**
 * Registers entries (Objects that are identifiable by a UUID)
 */
class Registry[T <% { def uuid: UUID }] {

    var entries: Map[UUID, T] = Map()


    def add(entry: T) { entries = entries + (entry.uuid -> entry) }

    def +=(entry: T) { add(entry) }

    def remove(uuid: UUID) { entries = entries - uuid }

    def remove(entry: T) { remove(entry.uuid) }


    def get(uuid: UUID) = entries.get(uuid)

    def getAll = entries.values
}