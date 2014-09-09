/**
 * For copyright information see the LICENSE document.
 */

package entice.server


/** Simple global application config settings */
trait Config { self: Core =>

  /** The actual config, should have been loaded from somewhere */
  lazy val config = {
    Config(
      self.app.configuration.getString("mongodb.uri").getOrElse("mongodb://localhost:27017/entice"),
      self.app.configuration.getString("frontend.url").getOrElse("entice.kallis.to"),
      self.app.configuration.getInt("server.tick").getOrElse(30),
      self.app.configuration.getInt("server.update.min").getOrElse(50),
      self.app.configuration.getInt("server.update.max").getOrElse(250)
    )
  }

  case class Config(
    mongodbUri: String,
    frontendUrl: String,
    tick: Int,
    minUpdate: Int,
    maxUpdate: Int)
}
