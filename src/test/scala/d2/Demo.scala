package d3

import unfiltered.request._
import unfiltered.response._

import scalaz._
import std.option._
import syntax.monad._
import syntax.std.option._


class Demo {

  val D = d2.Directives[Option]
  import D._
  import D.ops._


  val x = for {
    r <- request // uten type annotation vil denne gi Directive[Any, ...]
    a <- xGet | failure(MethodNotAllowed) // implicit fra syntax._
    o <- getOrElse(Option(""), MethodNotAllowed)
    if r.method == "GET" | MethodNotAllowed // filter syntax
  } yield a

  def xGet = for {
    _ <- GET
    r <- failure(BadRequest) | failure(Unauthorized)
  } yield r
}
