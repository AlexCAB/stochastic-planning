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

import scala.collection.Seq

package object errors

private def predicateAssert[F[_]: ApplicativeThrow, V](p: Boolean, v: V, msg: String): F[V] =
  if p then ApplicativeThrow[F].pure(v)
  else ApplicativeThrow[F].raiseError(AssertionError(msg))

extension (msg: String)
  inline def assertionError[F[_]: ApplicativeThrow, V]: F[V] = ApplicativeThrow[F].raiseError(AssertionError(msg))

extension [T, C[_] <: Seq[T]](seq: C[T])
  inline def assertDistinct[F[_]: ApplicativeThrow](msg: String): F[C[T]] =
    val duplicates = seq.groupBy(identity).filter((_, v) => v.size > 1).keySet
    predicateAssert(
      seq.distinct.size == seq.size,
      seq,
      msg + s", seq: ${seq.mkString(",")}, duplicates: ${duplicates.mkString(",")}"
    )

  inline def assertNonEmpty[F[_]: ApplicativeThrow](msg: String): F[C[T]] =
    predicateAssert(seq.nonEmpty, seq, msg + s", seq: ${seq.mkString(",")}")

  inline def assertUniform[F[_]: ApplicativeThrow](msg: String): F[C[T]] =
    predicateAssert(seq.isEmpty || (seq.distinct.size == 1), seq, msg + s", seq: ${seq.mkString(",")}")

extension [L, R, CL[_] <: IterableOnce[L], CR[_] <: IterableOnce[R]](value: (CL[L], CR[R]))
  inline def assertSameSize[F[_]: ApplicativeThrow](msg: String): F[(CL[L], CR[R])] = predicateAssert(
    value._1.iterator.size == value._2.iterator.size,
    value,
    msg + s", left size: ${value._1.iterator.size}, right size: ${value._2.iterator.size}"
  )

  inline def assertSameElems[F[_]: ApplicativeThrow](msg: String): F[(CL[L], CR[R])] = predicateAssert(
    value._1.iterator.toSet == value._2.iterator.toSet,
    value,
    msg + s", left collection: ${value._1.iterator.toSet}, right collection: ${value._2.iterator.toSet}"
  )

extension (bool: Boolean)
  inline def assertTrue[F[_]: ApplicativeThrow](msg: String): F[Unit] = predicateAssert(bool, (), msg)

extension (value: Long)
  inline def assetAnNumberOf[F[_]: ApplicativeThrow](msg: String): F[Unit] =
    predicateAssert(value >= 0, (), msg + s", expecter to be a number not null value, but got: $value")
