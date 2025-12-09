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

  inline def assertEmpty[F[_] : ApplicativeThrow](msg: String): F[C[T]] =
    predicateAssert(seq.isEmpty, seq, msg + s", seq: ${seq.mkString(",")}")

  inline def assertUniform[F[_]: ApplicativeThrow](msg: String): F[C[T]] =
    predicateAssert(seq.isEmpty || (seq.distinct.size == 1), seq, msg + s", seq: ${seq.mkString(",")}")

extension [L, R, CL[_] <: IterableOnce[L], CR[_] <: IterableOnce[R]](value: (CL[L], CR[R]))
  inline def assertSameSize[F[_]: ApplicativeThrow](msg: String): F[(CL[L], CR[R])] = predicateAssert(
    value._1.iterator.size == value._2.iterator.size,
    value,
    msg + s", left size: ${value._1.iterator.size}, right size: ${value._2.iterator.size}"
  )

extension [T, CL[_] <: IterableOnce[T], CR[_] <: IterableOnce[T]](value: (CL[T], CR[T]))
  inline def assertSameElems[F[_]: ApplicativeThrow](msg: String): F[(CL[T], CR[T])] = predicateAssert(
    value._1.iterator.toSet == value._2.iterator.toSet,
    value,
    msg + s", left collection: ${value._1.iterator.toSet}, right collection: ${value._2.iterator.toSet}"
  )

  inline def assertContainsAll[F[_]: ApplicativeThrow](msg: String): F[(CL[T], CR[T])] =
    val rightSet = value._2.iterator.toSet
    val dif = rightSet -- value._1
    predicateAssert(
      dif.isEmpty,
      value,
      msg + s", collection: ${value._1.iterator.toSet}, do not contains: $dif of collection $rightSet"
    )

  inline def assertNoSameElems[F[_]: ApplicativeThrow](msg: String): F[(CL[T], CR[T])] =
    val leftSet = value._1.iterator.toSet
    val rightSet = value._2.iterator.toSet
    val intersect = rightSet.intersect(leftSet)
    predicateAssert(
      intersect.isEmpty,
      value,
      msg + s", found same elements $intersect, in collections: $leftSet and $rightSet"
    )

extension (bool: Boolean)
  inline def assertTrue[F[_]: ApplicativeThrow](msg: String): F[Unit] = predicateAssert(bool, (), msg)

extension (value: Long)
  inline def assetAnNumberOf[F[_]: ApplicativeThrow](msg: String): F[Unit] =
    predicateAssert(value >= 0, (), msg + s", expecter to be a number not null value, but got: $value")
