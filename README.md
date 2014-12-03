d2
==
``Directive[-T, F[+_], +L, +R]`` defined for ``scalaz.Monad[F]`` where ``Result[+L, +R]``

scala.concurrent.Future
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

val DFuture = Directives[Future] // scalaz.Monad
import DFuture._
import ops._

def someFutureResponse: Future[String] = ???

def handleFoo = for {
  _   <- GET
  res <- someFutureResponse.valueSuccess
} yield Ok ~> ResponseString(res)
```
