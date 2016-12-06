package d2

import unfiltered.request.HttpRequest
import unfiltered.response.ResponseFunction

object Sync extends Sync[Any](PartialFunction.empty)

case class Sync[B](errorHandler:PartialFunction[Throwable, ResponseFunction[B]]) {
  type Id[+A] = A

  def id[A, BB <: B](pf:PartialFunction[HttpRequest[A], Directive[A, Id, ResponseFunction[BB], ResponseFunction[BB]]]): unfiltered.Cycle.Intent[A, BB] = {
    case req if pf.isDefinedAt(req)  => Result.merge(pf.apply(req).run(req))
  }

  case class Mapping[T, X](from: HttpRequest[T] => X) {
    def apply[TT <: T, BB <: B](intent: PartialFunction[X, Directive[TT, Id, ResponseFunction[BB], ResponseFunction[BB]]]) = id[TT, BB] {
      case req if intent.isDefinedAt(from(req)) => intent(from(req))
    }
  }

  val Path = Mapping[Any, String] {
    case unfiltered.request.Path(p) => p
  }
}
