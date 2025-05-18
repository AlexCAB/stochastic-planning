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
| created: 2025-03-20 |||||||||||*/

package planning.engine.common.errors

import cats.ApplicativeThrow

import scala.collection.AbstractSeq

package object errors

extension (msg: String)
  inline def assertionError[F[_]: ApplicativeThrow, V]: F[V] = ApplicativeThrow[F].raiseError(AssertionError(msg))

extension [T, C[_] <: AbstractSeq[T]](seq: C[T])
  inline def assertDistinct[F[_]: ApplicativeThrow](msg: String): F[C[T]] =
    if seq.distinct.size == seq.size then
      ApplicativeThrow[F].pure(seq)
    else
      ApplicativeThrow[F].raiseError(AssertionError(msg + s", seq: ${seq.mkString(",")}"))
