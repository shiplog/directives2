package d2

import unfiltered.request.HttpRequest
import unfiltered.response.{ContentLength, InternalServerError, ResponseFunction}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Async extends Async[Any](PartialFunction.empty)

case class Async[B](errorHandler:PartialFunction[Throwable, ResponseFunction[B]]) {

  def scalaFuture[A, BB <: B](pf:PartialFunction[HttpRequest[A], Directive[A, Future, ResponseFunction[BB], ResponseFunction[BB]]])(implicit ex:ExecutionContext):unfiltered.Async.Intent[A, BB] = {
    case req if pf.isDefinedAt(req) => pf.apply(req).run(req).onComplete{
      case Success(result)    => req.respond(Result.merge(result))
      case Failure(throwable) => req.respond(errorHandler.lift(throwable).getOrElse(InternalServerError ~> ContentLength("0")))
    }
  }

  case class Mapping[T, X](from: HttpRequest[T] => X) {
    def apply[TT <: T, BB <: B](intent: PartialFunction[X, Directive[TT, Future, ResponseFunction[BB], ResponseFunction[BB]]])(implicit ex:ExecutionContext) = scalaFuture[TT, BB] {
      case req if intent.isDefinedAt(from(req)) => intent(from(req))
    }
  }
}
