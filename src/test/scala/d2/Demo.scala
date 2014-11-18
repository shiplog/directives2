package d2

import unfiltered.request.{POST, GET}
import unfiltered.response.MethodNotAllowed

import scalaz._
import syntax.monad._
import std.option._


class Demo {

  val D = Directives[Option]
  import D._
  import D.syntax._


  val x = for {
    r <- request
    a <- GET
    if r.method == "GET" | MethodNotAllowed
  } yield a

}
