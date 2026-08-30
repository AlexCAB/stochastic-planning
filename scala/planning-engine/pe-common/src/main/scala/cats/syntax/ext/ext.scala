package cats.syntax

/*|||||||||||||||||||||||||||||||||
|| 0 * * * * * * * * * ▲ * * * * ||
|| * ||||||||||| * ||||||||||| * ||
|| * ||  * * * * * ||       || 0 ||
|| * ||||||||||| * ||||||||||| * ||
|| * * ▲ * * 0|| * ||   (< * * * ||
|| * ||||||||||| * ||  ||||||||||||
|| * * * * * * * * *   ||||||||||||
| author: CAB |||||||||||||||||||||
| website: github.com/alexcab |||||
| created: 24-Aug-26 |||||||||||*/

import cats.Monad
import cats.syntax.all.*

package object ext:

  inline def ifNonEmpty[F[_]: Monad, R](r: => R, first: IterableOnce[?], rest: IterableOnce[?]*)(f: => F[R]): F[R] =
    if first.iterator.nonEmpty || rest.exists(_.iterator.nonEmpty) then f else r.pure

  extension [E](it: IterableOnce[E])
    inline def foldM[F[_]: Monad, R](r: R)(f: (R, E) => F[R]): F[R] =
      it.iterator.foldLeft(r.pure[F])((acc, e) => acc.flatMap(f(_, e)))

  extension [A](it: Set[A])
    inline def traverse[F[_]: Monad, B](f: A => F[B]): F[Set[B]] =
      it.foldLeft(Set.empty[B].pure[F])((acc, e) => acc.flatMap(s => f(e).map(s + _)))

  extension [K, V](it: Map[K, V])
    inline def traverse[F[_]: Monad, K2, V2](f: (K, V) => F[(K2, V2)]): F[Map[K2, V2]] =
      it.foldLeft(Map.empty[K2, V2].pure[F])((acc, e) => acc.flatMap(s => f(e._1, e._2).map(s + _)))

    inline def traverseToSet[F[_]: Monad, R](f: (K, V) => F[R]): F[Set[R]] =
      it.foldLeft(Set.empty[R].pure[F])((acc, e) => acc.flatMap(s => f(e._1, e._2).map(s + _)))
