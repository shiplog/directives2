package d2

import unfiltered.request.HttpRequest
import scalaz._
import syntax.monad._

import scala.language.{higherKinds, implicitConversions}

object Result {
  case class Success[+A](value:A) extends Result[Nothing, A]
  case class Failure[+A](value:A) extends Result[A, Nothing]
  case class Error[+A](value:A) extends Result[A, Nothing]

  implicit def monad[L] = new Monad[({type X[A] = Result[L, A]})#X]{
    def bind[A, B](fa: Result[L, A])(f: (A) => Result[L, B]) = fa flatMap f
    def point[A](a: => A) = Success(a)
  }

  implicit def traverse[L] = new Traverse[({type X[A] = Result[L, A]})#X]{
    def traverseImpl[G[_], A, B](fa: Result[L, A])(f: (A) => G[B])(implicit G: Applicative[G]) =
      fa match {
        case Result.Success(value) => G.map(f(value))(Result.Success(_))
        case Result.Failure(value) => G.point(Result.Failure(value))
        case Result.Error(value)   => G.point(Result.Error(value))
      }
  }
}

sealed trait Result[+L, +R] {
  def flatMap[LL >: L, B](f:R => Result[LL, B]):Result[LL, B] = this match {
    case Result.Success(value) => f(value)
    case Result.Failure(value) => Result.Failure(value)
    case Result.Error(value)   => Result.Error(value)
  }

  def orElse[LL >: L, RR >: R](next:Result[LL, RR]):Result[LL, RR] = this match {
    case Result.Success(value) => Result.Success(value)
    case Result.Failure(_)     => next
    case Result.Error(value)   => Result.Error(value)
  }
}

object Directive {

  implicit def monad[T, F[+_] : Monad, L] = new Monad[({type X[A] = Directive[T, F, L, A]})#X]{
    def bind[A, B](fa: Directive[T, F, L, A])(f: (A) => Directive[T, F, L, B]) = fa flatMap f
    def point[A](a: => A) = Directive[Any, F, L, A](_ => Monad[F].point(Result.Success(a)))
  }

  def point[F[+_] : Monad, A](a: => A) = monad[Any, F, Nothing].point(a)

  def result[F[+_] : Monad, L, R](result: => Result[L, R]) = Directive[Any, F, L, R](_ => Monad[F].point(result))
  def success[F[+_] : Monad, R](success: => R) = result[F, Nothing, R](Result.Success(success))
  def failure[F[+_] : Monad, L](failure: => L) = result[F, L, Nothing](Result.Failure(failure))
  def error[F[+_] : Monad, L](error: => L) = result[F, L, Nothing](Result.Error(error))

  case class Filter[+L](result:Boolean, failure: () => L)
}

case class Directive[-T, F[+_], +L, +R](run:HttpRequest[T] => F[Result[L, R]]){
  def flatMap[TT <: T, LL >: L, B](f:R => Directive[TT, F, LL, B])(implicit F:Monad[F]) =
    Directive[TT, F, LL, B](req => run(req).flatMap{
      case Result.Success(value) => f(value).run(req)
      case Result.Failure(value) => F.point(Result.Failure(value))
      case Result.Error(value)   => F.point(Result.Error(value))
    })

  def map[B](f:R => B)(implicit F:Monad[F]) = flatMap[T, L, B](r => Directive.point[F, B](f(r)))

  def filter[LL >: L](f:R => Directive.Filter[LL])(implicit F:Monad[F]):Directive[T, F, LL, R] =
    flatMap{ r =>
      val result = f(r)
      if(result.result)
        Directive.success[F, R](r)
      else
        Directive.failure[F, LL](result.failure())
    }

  def withFilter[LL >: L](f:R => Directive.Filter[LL])(implicit F:Monad[F]) =
    filter(f)
}

object Directives {
  def apply[F[+_]](implicit M:Monad[F]):Directives[F] = new Directives[F]{
    protected implicit val F: Monad[F] = M
  }
}

trait Directives[F[+_]] {
  protected implicit val F:Monad[F]

  /* HttpRequest has to be of type Any because of type-inference (SLS 8.5) */
  case class when[R](f:PartialFunction[HttpRequest[Any], R]){
    def orElse[L](fail:L) =
      request[Any].flatMap(r => if(f.isDefinedAt(r)) Directive.success[F, R](f(r)) else Directive.failure[F, L](fail))
  }

  object syntax {
    implicit class FilterSyntax(b:Boolean) {
      def | [L](failure: => L) = Directive.Filter(b, () => failure)
    }
    implicit def MethodDirective(M:unfiltered.request.Method) = when{ case M(_) => M } orElse unfiltered.response.MethodNotAllowed
  }

  object request {
    def apply[T] = Directive[T, F, Nothing, HttpRequest[T]](req => F.point(Result.Success(req)))

    def underlying[T] = apply[T].map(_.underlying)
  }

  implicit def AnyRequest(r:request.type) = request[Any]
}
