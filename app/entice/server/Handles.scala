/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.utils.DualMap


/** All handles in here */
trait Handles {

  /** Thrown when trying to access/lookup a non-registered handle */
  case class HandleInvalidException() extends RuntimeException("""
      |Handle is invalid. Cannot lookup referenced data.
    """.stripMargin.trim)


  /** Thrown when trying to register a handle but can't create new IDs */
  case class HandlesExhaustedException() extends RuntimeException("""
      |Cannot create a new handle, apparently we ran out of valid ids.
    """.stripMargin.trim)


  /** This module contains a referenced data set and its registry */
  trait HandleModule {

    type Id
    type Data <: DataLike
    type Handle <: HandleLike
    def Handle(id: Id): Handle

    /** Generator for new IDs (every handle needs a locally unique id) */
    def generateId(): Id

    /**
     * General handle.
     * Handles are serializable identifiers for internal objects
     */
    trait HandleLike { self: Handle =>
      def id: Id

      def isValid: Boolean = registry.exists(id)
      def invalidate() = registry.remove(id)

      /** Hint: Non-static reference! Do not store this! */
      def data: Data = registry.retrieveData(id).getOrElse(throw HandleInvalidException())
      def apply() = data

      /** Hint: Non-static reference! Do not store this! */
      def getData: Option[Data] = registry.retrieveData(id)

      /** Update the underlying data-set without changing the handle */
      def update(data: Data) = registry.update(this, data)
    }

    /** Abstract data set */
    trait DataLike { self: Data =>
      def createHandle(): Handle = registry.store(self)
    }

    /** A generic handle registry */
    object registry {
      var entries: DualMap[Id, Data] = DualMap()

      def exists(id: Id) = entries.containsLeft(id)

      def store(data: Data): Handle = tryRegister(data)
      private def tryRegister(data: Data, tries: Int = 0): Handle = {
        if (tries > 1000) { throw HandlesExhaustedException() }
        val id = generateId()
        if (entries.containsLeft(id)) { tryRegister(data, tries + 1) }
        else {
          entries += (id -> data)
          Handle(id)
        }
      }

      def update(handle: Handle, data: Data): Handle = { entries += (handle.id -> data); handle }

      def retrieve(id: Id): Option[Handle] = entries.getLeft(id) map { _ => Handle(id) }

      def retrieveData(id: Id): Option[Data] = entries.getLeft(id)
      def retrieveHandle(data: Data): Option[Handle] = entries.getRight(data) map { id => Handle(id) }

      def remove(id: Id) { entries.removeLeft(id) }
    }
  }
}
