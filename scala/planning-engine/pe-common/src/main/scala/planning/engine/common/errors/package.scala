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

package object errors

private def predicateAssert[F[_]: ApplicativeThrow](p: Boolean, msg: String): F[Unit] =
  if p then ApplicativeThrow[F].unit
  else ApplicativeThrow[F].raiseError(AssertionError(msg))

extension (msg: String)
  inline def assertionError[F[_]: ApplicativeThrow, V]: F[V] = ApplicativeThrow[F].raiseError(AssertionError(msg))

extension (bool: Boolean)
  inline def assertTrue[F[_]: ApplicativeThrow](msg: String): F[Unit] = predicateAssert(bool, msg)

extension (value: Long)
  inline def assertAnNumberOf[F[_]: ApplicativeThrow](msg: String): F[Unit] =
    predicateAssert(value >= 0, msg + s", expecter to be a number not null value, but got: $value")

extension [L](left: L)
  inline def assertEqual[F[_]: ApplicativeThrow, R](right: R, msg: String): F[Unit] =
    predicateAssert(left == right, msg + s", left value: $left not equal to right value: $right")

extension [L](left: IterableOnce[L])
  inline def assertDistinct[F[_]: ApplicativeThrow](msg: String): F[Unit] =
    val dup = left.iterator.toSeq.groupBy(identity).filter((_, v) => v.size > 1).keySet
    predicateAssert(dup.isEmpty, msg + s", seq: ${left.iterator.mkString(",")}, duplicates: ${dup.mkString(",")}")

  inline def assertUniform[F[_]: ApplicativeThrow](msg: String): F[Unit] = predicateAssert(
    left.iterator.isEmpty || (left.iterator.toSet.size == 1),
    msg + s", seq: ${left.iterator.mkString(",")}"
  )

  inline def assertNonEmpty[F[_]: ApplicativeThrow](msg: String): F[Unit] =
    predicateAssert(left.iterator.nonEmpty, msg + s", seq: ${left.iterator.mkString(",")}")

  inline def assertEmpty[F[_]: ApplicativeThrow](msg: String): F[Unit] =
    predicateAssert(left.iterator.isEmpty, msg + s", seq: ${left.iterator.mkString(",")}")

  inline def assertNotContain[F[_]: ApplicativeThrow](right: L, msg: String): F[Unit] =
    predicateAssert(!left.iterator.contains(right), msg + s", seq: ${left.iterator.mkString(",")} contains: $right")

  inline def assertSameSize[F[_]: ApplicativeThrow, R](right: IterableOnce[R], msg: String): F[Unit] = predicateAssert(
    left.iterator.size == right.iterator.size,
    msg + s", left size: ${left.iterator.size}, right size: ${right.iterator.size}"
  )

  inline def assertSameElems[F[_]: ApplicativeThrow](right: IterableOnce[L], msg: String): F[Unit] = predicateAssert(
    left.iterator.toSet == right.iterator.toSet,
    msg + s", left seq: ${left.iterator.toSet}, right seq: ${right.iterator.toSet}"
  )

  inline def assertContainsAll[F[_]: ApplicativeThrow](right: IterableOnce[L], msg: String): F[Unit] =
    val rSet = right.iterator.toSet
    val dif = rSet -- left
    predicateAssert(dif.isEmpty, msg + s", seq: ${left.iterator.toSet}, do not contains: $dif of seq $rSet")

  inline def assertNoSameElems[F[_]: ApplicativeThrow](right: IterableOnce[L], msg: String): F[Unit] =
    val leftSet = left.iterator.toSet
    val rightSet = right.iterator.toSet
    val int = rightSet.intersect(leftSet)
    predicateAssert(int.isEmpty, msg + s", found same elements $int, in seq: $leftSet and $rightSet")
