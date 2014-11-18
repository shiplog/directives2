package d3

import unfiltered.request.{POST, GET}
import unfiltered.response.MethodNotAllowed

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
    a <- GET | POST // implicit fra syntax._
    o <- Option("").cata(Directive.success, Directive.failure(MethodNotAllowed)) // istedenfor gamle getOrElse directivet
    if r.method == "GET" | MethodNotAllowed // filter syntax
  } yield a

}
