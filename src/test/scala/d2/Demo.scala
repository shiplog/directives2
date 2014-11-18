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
  import D.syntax._


  val x = for {
    r <- request // uten type annotation vil denne gi Directive[Any, ...]
    a <- xGet | Directive.failure(MethodNotAllowed) // implicit fra syntax._
    o <- Option("").cata(Directive.success, Directive.failure(MethodNotAllowed)) // istedenfor gamle getOrElse directivet
    if r.method == "GET" | MethodNotAllowed // filter syntax
  } yield a

  val xGet = for {
    _ <- GET
    r <- Directive.failure(BadRequest) | Directive.failure(Unauthorized)
  } yield r
}
