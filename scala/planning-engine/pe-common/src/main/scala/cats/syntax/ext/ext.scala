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

  extension [E](it: Iterable[E])
    inline def foldU[F[_]: Monad, R](r: R)(f: (R, E) => F[R]): F[R] =
      it.foldLeft(r.pure[F])((acc, e) => acc.flatMap(f(_, e)))
