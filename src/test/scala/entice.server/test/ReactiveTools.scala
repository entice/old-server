/**
 * For copyright information see the LICENSE document.
 */

package entice.server
package test

import scala.util.{Try, Success, Failure}
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps


/** To be used with Futures. @See https://gist.github.com/sam/5155653 */
trait ReactiveTools {
  def whenReady[A](result:Future[A], timeout:Duration = 1 second)(expectation: A => Unit) = {
    expectation(Await.result(result, timeout))
  }
   
  def tryWhenReady[A](result:Future[Try[A]], timeout:Duration = 1 second)
                     (failure:Throwable => Unit)
                     (expectation: A => Unit) = {
    Await.result(result, timeout) match {
      case Failure(e) => failure(e)
      case Success(result:A) => expectation(result)
    }
  }
}


/*
"deleting a document" should {
  "be successful" in {
    tryWhenReady(User.create(scott))(fail) { userDoc =>
      tryWhenReady(User.delete(userDoc))(fail) { _ must be ('ok) }
    }
    
    whenReady(User.create(scott)) {
      case Failure(e) => fail(e)
      case Success(user) =>
        whenReady(User.delete(user)) {
          case Failure(e) => fail(e)
          case Success(result) => result must be ('ok)
        }
    }
  }
}
*/