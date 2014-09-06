/**
 * For copyright information see the LICENSE document.
 */

package entice.server

import entice.server.implementation.events._

import entice.protocol._
import entice.protocol.utils._

import akka.actor._
import akka.routing._
import akka.io.{ IO, Tcp, TcpPipelineHandler }
import akka.io.TcpPipelineHandler._

import scala.collection.JavaConversions._
import java.net.InetSocketAddress


trait Network {

  def serverHost: String
  def serverPort: Int

  def network: NetworkLike

  trait NetworkLike {
    def init()
    def shutdown()
  }
}
