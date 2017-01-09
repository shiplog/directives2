Unfiltered Directives 2
==
``Directive[-T, F[+_], +L, +R]`` defined for ``scalaz.Monad[F]`` where ``Result[+L, +R]``

[![Build Status](https://travis-ci.org/shiplog/directives2.svg)](https://travis-ci.org/shiplog/directives2)

Directives2 is cross-compiled for 2.10, 2.11 and 2.12 and the only dependencies are scalaz-core and unfiltered.

To include in your project:

``libraryDependencies += "no.shiplog" %% "directives2" % "0.10.0"``

For backwards compatibility, there are implicits converting from ``unfiltered.directives.Directive`` to ``d2.Directive``. Therefore, we added a dependency to:

``libraryDependencies += "ws.unfiltered" %% "unfiltered-directives" % "0.9.0-beta2" % "optional"``

Example using scala.concurrent.Future
---
```scala
val MappedAsyncIntent = d2.Async.Mapping[Any, String] {
  case unfiltered.filter.request.ContextPath(_, path) => path
}

new unfiltered.filter.async.Plan {
  val intent = MappedAsyncIntent {
    case "/foo" => handleFoo
  }
}

val DFuture = Directives[Future]
import DFuture._
import ops._
import scalaz._
import std.scalaFuture._

def someFutureResponse: Future[String] = ???

def handleFoo = for {
  _   <- GET
  res <- someFutureResponse.valueSuccess
} yield Ok ~> ResponseString(res)
```
