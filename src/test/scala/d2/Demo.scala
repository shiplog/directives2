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
  import D.implicits._

  val x: d2.Directive[Any, Option, Status, (String, String)] = for {
    r <- request // will give Directive[Any, ..] without HttpRequest-like type annotation
    a <- GET | POST // orElse operator. Will run second predicate if any failures (not errors) is rolled up from below. Will result in MethodNotAllowed if not committed.
    _ <- commit // commit after a combinator to prevent any failures rolling up resulting in a MethodNotAllowed
    o <- getOrElseF(Option(Option("")), MethodNotAllowed) // getOrElse on value. (Option-specific)
    f <- Option("foo").successValue // decorate with directive
    b <- Option("bar") // implicitly decorated by a Success projection from implicits._
  } yield f -> b

}
