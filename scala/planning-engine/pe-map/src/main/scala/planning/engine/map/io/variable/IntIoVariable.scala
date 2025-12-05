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

package planning.engine.map.io.variable

import cats.MonadThrow
import neotypes.model.types.Value
import planning.engine.common.errors.assertionError
import planning.engine.common.properties.*
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.common.values.io.IoIndex
import planning.engine.map.io.variable.IoVariable.*

abstract class IntIoVariableLike[F[_]: MonadThrow] extends IoVariable[F, Long]:
  def min: Long
  def max: Long
  def valueForIndex(index: IoIndex): F[Long]
  def indexForValue(value: Long): F[IoIndex]

final case class IntIoVariable[F[_]: MonadThrow](min: Long, max: Long) extends IntIoVariableLike[F]:

  override def valueForIndex(index: IoIndex): F[Long] = index.value match
    case v if v >= min && v <= max => v.pure
    case v                         => s"Value $v of index $index not in range: $min to $max".assertionError

  override def indexForValue(value: Long): F[IoIndex] = value match
    case v if v >= min && v <= max => IoIndex(v).pure
    case v                         => s"Value $v not in range: $min to $max".assertionError

  override def toQueryParams: F[Map[String, Param]] = paramsOf(
    PROP.VAR_TYPE -> PROP_VALUE.INT_TYPE.toDbParam,
    PROP.MIN -> min.toDbParam,
    PROP.MAX -> max.toDbParam
  )

  override def toString: String = s"IntIoVariable(min = $min, min = $max)"

object IntIoVariable:
  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[IntIoVariable[F]] =
    for
      min <- properties.getValue[F, Long](PROP.MIN)
      max <- properties.getValue[F, Long](PROP.MAX)
    yield IntIoVariable(min, max)
