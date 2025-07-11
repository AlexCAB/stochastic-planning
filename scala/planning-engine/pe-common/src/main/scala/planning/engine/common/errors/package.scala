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

  inline def assertNonEmpty[F[_]: ApplicativeThrow](msg: String): F[C[T]] =
    if seq.nonEmpty then
      ApplicativeThrow[F].pure(seq)
    else
      ApplicativeThrow[F].raiseError(AssertionError(msg + s", seq: ${seq.mkString(",")}"))

extension [L, R, CL[_] <: IterableOnce[L], CR[_] <: IterableOnce[R]](value: (CL[L], CR[R]))
  inline def assertSameSize[F[_]: ApplicativeThrow](msg: String): F[(CL[L], CR[R])] =
    if value._1.iterator.size == value._2.iterator.size then
      ApplicativeThrow[F].pure(value)
    else
      ApplicativeThrow[F].raiseError(
        AssertionError(msg + s", left size: ${value._1.iterator.size}, right size: ${value._2.iterator.size}")
      )

  inline def assertSameElems[F[_]: ApplicativeThrow](msg: String): F[(CL[L], CR[R])] =
    if value._1.iterator.toSet == value._2.iterator.toSet then
      ApplicativeThrow[F].pure(value)
    else
      ApplicativeThrow[F].raiseError(
        AssertionError(
          msg + s", left collection: ${value._1.iterator.toSet}, right collection: ${value._2.iterator.toSet}"
        )
      )

extension (bool: Boolean)
  inline def assertTrue[F[_]: ApplicativeThrow](msg: String): F[Unit] =
    if bool then ApplicativeThrow[F].unit
    else ApplicativeThrow[F].raiseError(AssertionError(msg))

extension (value: Long)
  inline def assetAnNumberOf[F[_]: ApplicativeThrow](msg: String): F[Unit] =
    if value > 0 then ApplicativeThrow[F].unit
    else
      ApplicativeThrow[F].raiseError(AssertionError(msg + s", expecter to be positive not null value, but got: $value"))
