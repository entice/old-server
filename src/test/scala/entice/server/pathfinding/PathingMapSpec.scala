/**
 * For copyright information see the LICENSE document.
 */
 
package entice.server.pathfinding

import entice.protocol.Coord2D

import org.scalatest._
import org.scalatest.matchers._


class PathinMapSpec 
    extends WordSpec 
    with MustMatchers
    with OneInstancePerTest  {

    "A pathing-map" must {


        "load from a given file" in {
            ???
            // create the file

            // load it

            // delete the file
        }


        "get the trapezoid of a position" in {
            ???
            // load the pmap

            // select a valid position

            // check if it returns the right trapezoid

            // select an invalid position

            // check if it returns None
        }


        "get the farthest point reachable from a position" in {
            ???
            // load the pmap

            // select a valid position and direction that points
            // to a border in the current trapezoid

            // check if it returns the correct border-point

            // select a valid position and direction that points
            // to a border in another trapezoid (trough connections to it)

            // check if it returns the correct border-point

            // select an invalid position
        }
    }
}