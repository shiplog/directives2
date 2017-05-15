package d2
import scala.language.{higherKinds, implicitConversions}

import cats.Monad

trait Directives1Interop[F[+_]] {
  implicit def fromUnfilteredDirective[T, L, R](d1: unfiltered.directives.Directive[T, L, R])(implicit F: Monad[F]): d2.Directive[T, F, L, R] = {
    import unfiltered.directives.{Result => Res}
    Directive[T, F, L, R] { req =>
      d1(req) match {
        case Res.Success(s) => F.pure(Result.Success(s))
        case Res.Failure(e) => F.pure(Result.Failure(e))
        case Res.Error(e) => F.pure(Result.Error(e))
      }
    }
  }
}

object Directives1Interop {
  def apply[F[+_]](implicit M:Monad[F]): Directives[F] = new Directives[F] with Directives1Interop[F] {
    implicit val F: Monad[F] = M
  }
}
