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
| created: 04-Sep-26 |||||||||||*/

package planning.engine.planner.mpi.model.io

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.io.IoIndex
import planning.engine.common.errors.assertionError

trait Type[V]:
  def isDefinedAt(index: IoIndex): Boolean
  def valueForIndex[F[_]: MonadThrow](index: IoIndex): F[V]
  def indexForValue[F[_]: MonadThrow](value: V): F[IoIndex]

object Type:
  final case class N(min: Long, max: Long) extends Type[Long]:
    override def isDefinedAt(index: IoIndex): Boolean = min <= index.value && index.value <= max

    override def valueForIndex[F[_]: MonadThrow](index: IoIndex): F[Long] =
      if isDefinedAt(index) then index.value.pure
      else s"Index $index not in range [$min, $max]".assertionError

    override def indexForValue[F[_]: MonadThrow](value: Long): F[IoIndex] =
      if value >= min && value <= max then IoIndex(value).pure
      else s"Value $value not in range [$min, $max]".assertionError

    override def toString: String = s"[$min, $max]"

  final case class R(min: Double, max: Double) extends Type[Double]:
    private val scaling = 10000.0

    private def toValue(index: IoIndex): Double = index.value.toDouble / scaling

    override def isDefinedAt(index: IoIndex): Boolean =
      val value = toValue(index)
      min <= value && value <= max

    override def valueForIndex[F[_]: MonadThrow](index: IoIndex): F[Double] =
      val value = toValue(index)
      if isDefinedAt(index) then value.pure
      else s"Value $value not in range [$min, $max], for index $index".assertionError

    override def toString: String = s"[$min, $max]"

    override def indexForValue[F[_]: MonadThrow](value: Double): F[IoIndex] =
      if value >= min && value <= max then IoIndex((value * scaling).toLong).pure
      else s"Value $value not in range [$min, $max]".assertionError

  final case class Bool(acceptable: Set[Boolean]) extends Type[Boolean]:
    def isDefinedAt(index: IoIndex): Boolean = index.value match
      case 0 => acceptable.contains(false)
      case 1 => acceptable.contains(true)
      case _ => false

    def valueForIndex[F[_]: MonadThrow](index: IoIndex): F[Boolean] = index.value match
      case 0 if acceptable.contains(false) => false.pure
      case 1 if acceptable.contains(true)  => true.pure
      case _ => s"Invalid index ($index) or not in acceptable values: $acceptable".assertionError

    def indexForValue[F[_]: MonadThrow](value: Boolean): F[IoIndex] =
      if acceptable.contains(value) then IoIndex(if value then 1 else 0).pure
      else s"Value '$value' not in acceptable values: $acceptable".assertionError

    override def toString: String = s"{${acceptable.mkString(", ")}}"

  final case class Opt(options: List[String]) extends Type[String]:
    override def isDefinedAt(index: IoIndex): Boolean =
      index.value <= Int.MaxValue && options.isDefinedAt(index.value.toInt)

    override def valueForIndex[F[_]: MonadThrow](index: IoIndex): F[String] =
      if isDefinedAt(index) then options(index.value.toInt).pure
      else s"Index $index out of bounds for options list of size ${options.size}".assertionError

    override def indexForValue[F[_]: MonadThrow](value: String): F[IoIndex] = options.indexOf(value) match
      case -1 => s"Value $value not in options list: $options".assertionError
      case i  => IoIndex(i).pure

    override def toString: String = s"{${options.mkString(", ")}}"
