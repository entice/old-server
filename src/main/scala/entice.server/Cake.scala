/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.macros._

import entice.server.implementation.cores._
import entice.server.implementation.events._
import entice.server.implementation.loggers._
import entice.server.implementation.networks._

import entice.protocol._

import akka.actor.ActorDSL._

import scala.io.StdIn.readLine


/**
 * This is the server. Period.
 */
object Main
    extends App
    with DefaultServer {

  logger.info(s"Starting a ${environment.toString.toLowerCase} server on: ${serverHost}:${serverPort}")

  core.init()
  network.init()

  implicit val system = actorSystem
  implicit val echo = actor(new Act {
    become { case msg @ Evt(_) => msg.sender ! msg.message }
  })

  eventBus.sub[Message]

  // wait till shutdown
  while (readLine != "exit") {}
  network.shutdown()
  core.shutdown()
}


/** A default server cake with default components */
trait DefaultServer
    extends DefaultLogger
    with Environment
    with DefaultCore
    with DefaultNetwork {

  lazy val serverHost = Option(System.getProperty("server.host"))
    .getOrElse(throw new NullPointerException("Property server.host has not been set."))

  lazy val serverPort = Option(System.getProperty("server.port"))
    .getOrElse(throw new NullPointerException("Property server.port has not been set.")).toInt

  lazy val environment = environmentFromProperty(Option(System.getProperty("app.env"))
    .getOrElse(throw new NullPointerException("Property app.env has not been set."))
  )
}
