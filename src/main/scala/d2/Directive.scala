package d2

import scala.annotation.tailrec

import unfiltered.request.HttpRequest
import scala.language.{higherKinds, implicitConversions, reflectiveCalls}

import cats._
import cats.implicits._
import unfiltered.response.MethodNotAllowed

object Result {

  def merge[A](result:Result[A, A]) = result match {
    case Success(value) => value
    case Failure(value) => value
    case Error(value) => value

  }

  case class Success[+A](value:A) extends Result[Nothing, A]
  case class Failure[+A](value:A) extends Result[A, Nothing]
  case class Error[+A](value:A) extends Result[A, Nothing]

  implicit def monad[L] = new Monad[({type X[A] = Result[L, A]})#X]{

    def pure[A](a: A): Result[L, A] = Success(a)

    def flatMap[A, B](fa: Result[L, A])(f: (A) => Result[L, B]): Result[L, B] = fa.flatMap(f)

    @tailrec
    def tailRecM[A, B](a: A)(f: (A) => Result[L, Either[A, B]]): Result[L, B] = f(a) match {
      case Result.Failure(value)           => Result.Failure(value)
      case Result.Error(value)             => Result.Error(value)
      case Result.Success(Right(value))    => Result.Success(value)
      case Result.Success(Left(nextValue)) => tailRecM(nextValue)(f)
    }
  }

  implicit def traverse[L] = new Traverse[({type X[A] = Result[L, A]})#X]{

    def traverse[G[_], A, B](fa: Result[L, A])(f: (A) => G[B])(implicit G: Applicative[G]): G[Result[L, B]] =
      fa match {
        case Result.Success(value) => G.map(f(value))(Result.Success(_))
        case Result.Failure(value) => G.pure(Result.Failure(value))
        case Result.Error(value)   => G.pure(Result.Error(value))
      }


    def foldLeft[A, B](fa: Result[L, A], b: B)(f: (B, A) => B): B = fa match {
      case Result.Success(a) => f(b, a)
      case _ => b
    }

    def foldRight[A, B](fa: Result[L, A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = fa match {
      case Result.Success(a) => f(a, lb)
      case _ => lb
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

  def map[B](f:R => B) = flatMap(r => Result.Success(f(r)))
}

object Directive {

  implicit def monad[T, F[+_] : Monad, L] = new Monad[({type X[A] = Directive[T, F, L, A]})#X] {
    implicit val F: FlatMap[F] = implicitly[FlatMap[F]]

    def flatMap[A, B](fa: Directive[T, F, L, A])(f: (A) => Directive[T, F, L, B]) = fa flatMap f

    def pure[A](a: A) = Directive[Any, F, L, A](_ => Monad[F].pure(Result.Success(a)))

    @tailrec
    override def tailRecM[A, B](a: A)(f: (A) => Directive[T, F, L, Either[A, B]]): Directive[T, F, L, B] =
      tailRecM(a)(a0 => Directive(f(a0).run))
  }

  def pure[F[+_] : Monad, A](a: => A) = monad[Any, F, Nothing].pure(a)

  def result[F[+_] : Monad, L, R](result: => Result[L, R]) = Directive[Any, F, L, R](_ => Monad[F].pure(result))
  def success[F[+_] : Monad, R](success: => R) = result[F, Nothing, R](Result.Success(success))
  def failure[F[+_] : Monad, L](failure: => L) = result[F, L, Nothing](Result.Failure(failure))
  def error[F[+_] : Monad, L](error: => L) = result[F, L, Nothing](Result.Error(error))

  object commit {
    def flatMap[T, F[+_]:Monad, R, A](f:Unit => Directive[T, F, R, A]):Directive[T, F, R, A] =
      commit(f(()))

    def apply[T, F[+_]:Monad, R, A](d:Directive[T, F, R, A]) = Directive[T, F, R, A]{ r => d.run(r).map{
      case Result.Failure(response) => Result.Error[R](response)
      case result                   => result
    }}
  }

  case class Filter[+L](result:Boolean, failure: () => L)
}

case class Directive[-T, F[+_], +L, +R](run:HttpRequest[T] => F[Result[L, R]]){
  def flatMap[TT <: T, LL >: L, B](f:R => Directive[TT, F, LL, B])(implicit F:Monad[F]) =
    Directive[TT, F, LL, B](req => run(req).flatMap{
      case Result.Success(value) => f(value).run(req)
      case Result.Failure(value) => F.pure(Result.Failure(value))
      case Result.Error(value)   => F.pure(Result.Error(value))
    })

  def map[B](f:R => B)(implicit F:Functor[F]) = Directive[T, F, L, B](req => run(req).map(_.map(f)))

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

  def orElse[TT <: T, LL >: L, RR >: R](next:Directive[TT, F, LL, RR])(implicit F:Monad[F]) =
    Directive[TT, F, LL, RR](req => run(req).flatMap{
      case Result.Success(value) => Result.Success(value).pure[F]
      case Result.Failure(_)     => next.run(req)
      case Result.Error(value)   => Result.Error(value).pure[F]
    })

  def | [TT <: T, LL >: L, RR >: R](next:Directive[TT, F, LL, RR])(implicit F:Monad[F]) = orElse(next)
}

object Directives {
  def apply[F[+_]](implicit M:Monad[F]):Directives[F] = new Directives[F]{
    implicit val F: Monad[F] = M
  }
}

trait Directives[F[+_]] {
  implicit val F:Monad[F]

  type Result[+L, +R] = d2.Result[L, R]
  val Result          = d2.Result

  type Directive[-T, +L, +R] = d2.Directive[T, F, L, R]

  object Directive {
    def apply[T, L, R](run:HttpRequest[T] => F[Result[L, R]]):Directive[T, L, R] = d2.Directive[T, F, L, R](run)
  }

  def result[L, R](result: Result[L, R]) = d2.Directive.result[F, L, R](result)
  def success[R](success: R) = d2.Directive.success[F, R](success)
  def failure[L](failure: L) = d2.Directive.failure[F, L](failure)
  def error[L](error: L)     = d2.Directive.error[F, L](error)

  def getOrElseF[L, R](opt:F[Option[R]], orElse: => L) = d2.Directive[Any, F, L, R] { _ =>
    opt.map(_.fold[Result[L, R]](Result.Failure(orElse))(Result.Success(_)))
  }

  def getOrElse[A, L](opt:Option[A], orElse: => L) = opt match {
    case Some(r) => success(r)
    case None => failure(orElse)
  }

  type Filter[+L] = d2.Directive.Filter[L]
  val Filter      = d2.Directive.Filter

  val commit = d2.Directive.commit

  def value[L, R](f: F[Result[L, R]]) = Directive[Any, L, R](_ => f)

  implicit def DirectiveMonad[T, L] = d2.Directive.monad[T, F, L]

  /* HttpRequest has to be of type Any because of type-inference (SLS 8.5) */
  case class when[R](f:PartialFunction[HttpRequest[Any], R]){
    def orElse[L](fail: => L) =
      request[Any].flatMap(r => f.lift(r) match {
        case Some(r) => success(r)
        case None => failure(fail)
      })
  }

  object ops {

    import unfiltered.request._

    implicit class FilterSyntax(b:Boolean) {
      def | [L](failure: => L) = Filter(b, () => failure)
    }

    implicit def MethodDirective(M:unfiltered.request.Method): d2.Directive[Any, F, MethodNotAllowed.type, Method] =
      when { case M(_) => M } orElse unfiltered.response.MethodNotAllowed

    implicit class MonadDecorator[+X](f: F[X]) {
      def successValue = d2.Directive[Any, F, Nothing, X](_ => f.map(Result.Success(_)))
      def failureValue = d2.Directive[Any, F, X, Nothing](_ => f.map(Result.Failure(_)))
      def errorValue   = d2.Directive[Any, F, X, Nothing](_ => f.map(Result.Error(_)))
    }

    implicit def queryParamsDirective[A, L](t: QueryParams.type): d2.Directive[A, F, L, Map[String, Seq[String]]] = {
      request[A].map{case QueryParams(qp) => qp}
    }

    implicit def stringHeaderDirective[A, L](Header: StringHeader): d2.Directive[A, F, L, Option[String]] = {
      request[A].map{
        case Header(v) => Some(v)
        case _ => None
      }
    }
  }

  object implicits {
    implicit def wrapSuccess[S](f: F[S]): d2.Directive[Any, F, Nothing, S] = ops.MonadDecorator(f).successValue
  }

  object request {
    def apply[T] = Directive[T, Nothing, HttpRequest[T]](req => F.pure(Result.Success(req)))

    def underlying[T] = apply[T].map(_.underlying)
  }

  implicit def AnyRequest(r:request.type) = request[Any]
}
